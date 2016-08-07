package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import me.anon.controller.adapter.PlantSelectionAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Garden;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class GardenDialogFragment extends DialogFragment
{
	private Garden garden;
	private PlantSelectionAdapter adapter;
	@Views.InjectView(R.id.name) private EditText name;
	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;

	@SuppressLint("ValidFragment")
	public GardenDialogFragment(Garden garden)
	{
		this.garden = garden;
	}

	@SuppressLint("ValidFragment")
	public GardenDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.garden_dialog_view, null, false);
		Views.inject(this, view);

		adapter = new PlantSelectionAdapter(PlantManager.getInstance().getSortedPlantList(), garden == null ? null : garden.getPlantIds(), getActivity());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

		return new AlertDialog.Builder(getActivity())
			.setTitle("Garden")
			.setView(view)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{

				}
			})
			.setNeutralButton("Select All", new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{

				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.dismiss();
				}
			}).create();
	}
}
