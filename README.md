# Advanced usage of DiffUtil with Kotlin and RxJava2
![](https://cdn-images-1.medium.com/max/800/1*GTPf4NHoy03Q40h0e1Denw.png)

## Read article [here](https://medium.com/proandroiddev/advanced-usage-of-diffutil-with-kotlin-and-rxjava2-2622e08b552b)
If you have a list that needs to be updated sometimes, you have to use DiffUtil. It’s a utility class that can calculate
the difference between two lists and update only the necessary items or its content. To do this in a right way, DiffUtil
uses notifyItem*  methods of RecycleView.Adapter.
The calculations can take some time, so it would be better to use a separate thread to prevent blocking of UI. When I faced 
this problem, that described above, I have found a lot of articles that contain examples of only basic usage. Thus, I decided 
to write this article and show how to update only specific part of item asynchronously.

![](https://cdn-images-1.medium.com/max/800/1*OQjank6RrIYeY96LW_t0eQ.gif)

It’s a simple app that contains one screen with a list of timezones and appropriate times. 
Each item shows the name, hours, minutes and seconds. Every second app gets the current time from different 
instances of Calendar class and updates only those TextViews that need it.

I chose Kotlin as a language that allows faster and safer app creation. For me, writing with its modern syntax is a pleasure. I also
chose RxJava2 to deal with async.

The project contains a base class for RecyclerView.Adapter, that encapsulates default logic of one:
```Kotlin
abstract class BaseAdapter<D, VH : BaseViewHolder<D>> : RecyclerView.Adapter<VH>() {

    var dataSource: List<D> = emptyList()

    override fun getItemCount() = dataSource.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(getItemViewId(), parent, false)
        return instantiateViewHolder(view)
    }

    abstract fun getItemViewId() : Int

    abstract fun instantiateViewHolder(view: View?): VH

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    fun getItem(position: Int) = dataSource[position]
}
```
Now we can inherit from this class to implement usual adapter with a minimum amount of code. Another benefit of this approach is an
ability to focus on the main logic of DiffUtil.
The DiffUtil.Callback is an abstract class as follows:
```Kotlin

 public abstract static class Callback {
   
        public abstract int getOldListSize();
   
        public abstract int getNewListSize();

        public abstract boolean areItemsTheSame(int oldItemPosition, int newItemPosition);

        public abstract boolean areContentsTheSame(int oldItemPosition, int newItemPosition);

        @Nullable
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return null;
        }
}
```
Names of getOldListSize() and getNewListSize() methods speaks for themselves. Usually, areItemsTheSame() gets old and new items,
compares their ids and returns the appropriate result:
```Kotlin
override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
        = oldList[oldItemPosition].id == newList[newItemPosition].id

override fun getOldListSize(): Int = oldList.size

override fun getNewListSize(): Int = newList.size
```
The method areContentTheSame() compares data from items:
```Kotlin
override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val new = newList[newItemPosition]
        val old = oldList[oldItemPosition]
        val isHoursTheSame = new.h == old.h
        val isMinutesTheSame = new.m == old.m
        val isSecondsTheSame = new.s == old.s
        return isHoursTheSame && isMinutesTheSame && isSecondsTheSame
}
```
If areItemsTheSame() returns true and areContentTheSame() returns false, DiffUtil invokes getChangePayload(). This method services to define
which content was changed. You can implement it like this:
```Kotlin
companion object {
        const val HOURS = "HOURS"
        const val MINUTES = "MINUTES"
        const val SECONDS = "SECONDS"
}

override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val new = newList[newItemPosition]
        val old = oldList[oldItemPosition]
        val set = mutableSetOf<String>()
        val isHoursTheSame = new.h == old.h
        val isMinutesTheSame = new.m == old.m
        val isSecondsTheSame = new.s == old.s
        if(isHoursTheSame.not()) {
            set.add(HOURS)
        }
        if(isMinutesTheSame.not()) {
            set.add(MINUTES)
        }
        if(isSecondsTheSame.not()) {
            set.add(SECONDS)
        }
        return set
}
```
You can choose any type of data as a result. The scheme below explains how DiffUtil compares lists.

![](https://cdn-images-1.medium.com/max/800/1*AyI1eccnCJ1mGxvYZqvHtQ.jpeg)

To handle difference between old and new content of the item, we have to override a method 
onBindViewHolder(VH holder, int position, List<Object> payloads). It is invoked after the call 
to notifyItemChanged(int, Object) or notifyItemRangeChanged(int, int, Object):
```Kotlin
override fun onBindViewHolder(holder: TimeViewHolder?, position: Int, payloads: MutableList<Any>?) {
        if(payloads?.isEmpty() ?: true) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val set = payloads?.firstOrNull() as Set<String>?
            set?.forEach {
                when(it) {
                    TimeDiffCallback.HOURS -> {
                        holder?.tvHours?.setTime(getItem(position).h)
                    }
                    TimeDiffCallback.MINUTES -> {
                        holder?.tvMinutes?.setTime(getItem(position).m)
                    }
                    TimeDiffCallback.SECONDS -> {
                        holder?.tvSeconds?.setTime(getItem(position).s)
                    }
                    else -> super.onBindViewHolder(holder, position, payloads)
                }
            }
        }
}
```
Payloads contains some info we can use to update only part of the item. If we detect a case when payloads is empty,
we just call super method and it, in turn, invokes onBindViewHolder(VH holder, int position). It contains the logic 
necessary to perform simple data binding to an instance of ViewHolder. Please notice, that 
onBindViewHolder(VH holder, int position) invokes only if we call super of 
onBindViewHolder(VH holder, int position, List<Object> payloads).
The method setDataSource() of the adapter takes an instance of Flowable, uses DiffUtil to calculate the difference between
old and new lists and returns Disposable to deal with Activity lifecycle:
```Kotlin
fun setDataSource(flowable: Flowable<List<Time>>) : Disposable {
        var newList: List<Time> = emptyList()
        return flowable
                .doOnNext { newList = it }
                .map { DiffUtil.calculateDiff(TimeDiffCallback(dataSource, it)) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { dataSource = newList }
                .subscribe { it.dispatchUpdatesTo(this) }
}
```
To synchronously highlight dividers I just use ValueAnimator and UpdateListener:
```Kotlin
companion object {
        @JvmStatic
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.REVERSE
            this.duration = 400
            this.start()
        }
}
......
  class TimeViewHolder(itemView: View?) : BaseViewHolder<Time>(itemView) {

        val vFirstDivider by lazy { itemView?.findViewById(R.id.vFirstDivider) }
        val vSecondDivider by lazy { itemView?.findViewById(R.id.vSecondDivider) }

        init {
            valueAnimator.addUpdateListener {
                vFirstDivider?.alpha = it.animatedFraction
                vSecondDivider?.alpha = it.animatedFraction
            }
        }
......
```
## Conclusions
DiffUtil is a really useful tool that allows to use RecyclerView in a simple and right way.
How you have probably noticed, it is very easy in use and it assumes all logic of calculations, 
that can be complex. Thus, using DiffUtil, you can prevent many bugs and make code  clearer. You
can also make DiffUtil async using, for instance, RxJava or default Thread. The sample project contains processing
of large volume of data and, as you can see, DiffUtil deals with this pretty fast.



