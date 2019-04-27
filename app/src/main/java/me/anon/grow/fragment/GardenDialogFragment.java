package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import me.anon.controller.adapter.PlantSelectionAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Garden;
import me.anon.model.Plant;

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
	public interface OnEditGardenListener
	{
		public void onGardenEdited(Garden garden);
	}

	private Garden garden;
	private PlantSelectionAdapter adapter;
	@Views.InjectView(R.id.name) private EditText name;
	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;
	private OnEditGardenListener onEditGardenListener;

	public void setOnEditGardenListener(OnEditGardenListener onEditGardenListener)
	{
		this.onEditGardenListener = onEditGardenListener;
	}

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

		if (garden != null)
		{
			name.setText(garden.getName());
		}

		adapter = new PlantSelectionAdapter(PlantManager.getInstance().getSortedPlantList(null), garden == null ? null : garden.getPlantIds(), getActivity());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
			.setTitle("Garden")
			.setView(view)
			.setPositiveButton("Ok", null)
			.setNeutralButton("Select All", null)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.dismiss();
				}
			}).create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override public void onShow(DialogInterface dialogInterface)
			{
				alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						if (TextUtils.isEmpty(name.getText()))
						{
							name.setError("Field is required");
							return;
						}

						if (garden == null)
						{
							garden = new Garden();
						}

						garden.setName(name.getText().toString());
						garden.setPlantIds(adapter.getSelectedIds());

						if (onEditGardenListener != null)
						{
							onEditGardenListener.onGardenEdited(garden);
						}

						alertDialog.dismiss();
					}
				});

				alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						view.setTag(view.getTag() == null || !(boolean)view.getTag());

						if ((boolean)view.getTag())
						{
							ArrayList<String> plantIds = new ArrayList<String>();
							for (Plant plant : adapter.getPlants())
							{
								plantIds.add(plant.getId());
							}

							adapter.setSelectedIds(plantIds);
							adapter.notifyDataSetChanged();
							((TextView)view).setText("Select none");
						}
						else
						{
							ArrayList<String> plantIds = new ArrayList<String>();

							adapter.setSelectedIds(plantIds);
							adapter.notifyDataSetChanged();
							((TextView)view).setText("Select all");
						}
					}
				});
			}
		});

		return alertDialog;
	}
}
