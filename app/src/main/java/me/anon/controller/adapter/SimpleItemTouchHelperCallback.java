package me.anon.controller.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback
{
	private final ItemTouchHelperAdapter mAdapter;

	public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter)
	{
		mAdapter = adapter;
	}

	@Override public boolean isLongPressDragEnabled()
	{
		return true;
	}

	@Override public boolean isItemViewSwipeEnabled()
	{
		return false;
	}

	@Override public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
	{
		int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
		int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
		return makeMovementFlags(dragFlags, swipeFlags);
	}

	@Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
	{
		mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
		return true;
	}

	@Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
	{
		mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
	}
}
