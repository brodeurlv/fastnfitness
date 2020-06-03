package com.easyfitness.programs;

/**
 * Called when a long click was done on an item in a RecyclerView
 */

public interface IOnRecyclerItemLongClick {
    /**
     * @param itemPosition position of the item in the adapter
     */
    void onItemLongClick(int itemPosition);
}
