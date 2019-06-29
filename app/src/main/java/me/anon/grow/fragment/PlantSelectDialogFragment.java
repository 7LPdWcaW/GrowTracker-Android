package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;

import me.anon.controller.adapter.PlantSelectionAdapter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;
import me.anon.view.PlantSelectHolder;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantSelectDialogFragment extends DialogFragment
{
	public interface OnDialogActionListener
	{
		/**
		 * @param plantIndex Indexes are indexes of plants defined in the {@link PlantManager#getPlants()} array
		 * @param showImage
		 */
		public void onDialogAccept(ArrayList<Integer> plantIndex, boolean showImage);
	}

	private boolean allowMultiple = false;
	private PlantSelectionAdapter adapter;
	@Views.InjectView(R.id.recycler_view) private RecyclerView recyclerView;
	private boolean showImages = true;
	private ArrayList<Integer> disabled = new ArrayList<>();
	private OnDialogActionListener onDialogActionListener;
	private DialogInterface.OnDismissListener onDismissListener;

	public void setOnDialogActionListener(OnDialogActionListener onDialogActionListener)
	{
		this.onDialogActionListener = onDialogActionListener;
	}

	public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener)
	{
		this.onDismissListener = onDismissListener;
	}

	@SuppressLint("ValidFragment")
	public PlantSelectDialogFragment()
	{
		this(false);
	}

	@SuppressLint("ValidFragment")
	public PlantSelectDialogFragment(boolean multiSelect)
	{
		this.allowMultiple = multiSelect;
	}

	public void setDisabled(Integer... disabled)
	{
		this.disabled.addAll(Arrays.asList(disabled));
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.plant_list_dialog_view, null, false);
		Views.inject(this, view);

		adapter = new PlantSelectionAdapter(PlantManager.getInstance().getSortedPlantList(null), null, getActivity())
		{
			@Override public void onBindViewHolder(PlantSelectHolder holder, final int position)
			{
				super.onBindViewHolder(holder, position);

				final Plant plant = getPlants().get(position);
				if (!showImages)
				{
					ImageLoader.getInstance().cancelDisplayTask(holder.getImage());
					holder.getImage().setImageDrawable(null);
				}

				holder.getCheckbox().setEnabled(true);
				holder.getCheckbox().setChecked(getSelectedIds().indexOf(plant.getId()) > -1);
				holder.itemView.setEnabled(true);
				holder.itemView.setAlpha(1.0f);

				if (disabled.indexOf(position) > -1)
				{
					holder.getCheckbox().setEnabled(false);
					holder.getCheckbox().setChecked(true);
					holder.itemView.setEnabled(false);
					holder.itemView.setAlpha(0.6f);
				}

				holder.itemView.setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						boolean check = !((CheckBox)view.findViewById(R.id.checkbox)).isChecked();
						((CheckBox)view.findViewById(R.id.checkbox)).setChecked(check);

						if (check)
						{
							if (!allowMultiple)
							{
								getSelectedIds().clear();
							}

							getSelectedIds().add(plant.getId());
						}
						else
						{
							getSelectedIds().remove(plant.getId());
						}

						adapter.notifyDataSetChanged();
					}
				});
			}
		};

		recyclerView.setAdapter(adapter);
		boolean reverse = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("reverse_order", false);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, reverse);
		layoutManager.setStackFromEnd(reverse);
		recyclerView.setLayoutManager(layoutManager);

		final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
			.setTitle("Select plant")
			.setView(view)
			.setPositiveButton("Ok", null)
			.setNeutralButton("Hide images", null)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.dismiss();
				}
			}).create();

		alertDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override public void onShow(final DialogInterface dialogInterface)
			{
				alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						if (onDialogActionListener != null)
						{
							if (adapter.getSelectedIds().size() == 0) return;

							ArrayList<Integer> plantIndex = new ArrayList<Integer>();

							for (int index = 0; index < PlantManager.getInstance().getPlants().size(); index++)
							{
								for (int adapterIndex = 0; adapterIndex < adapter.getSelectedIds().size(); adapterIndex++)
								{
									if (PlantManager.getInstance().getPlants().get(index).getId().equalsIgnoreCase(adapter.getSelectedIds().get(adapterIndex)))
									{
										plantIndex.add(index);
										break;
									}
								}
							}

							onDialogActionListener.onDialogAccept(plantIndex, showImages);
						}

						alertDialog.dismiss();
					}
				});

				alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View v)
					{
						alertDialog.dismiss();

						if (onDismissListener != null)
						{
							onDismissListener.onDismiss(dialogInterface);
						}
					}
				});

				alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View view)
					{
						showImages = !showImages;
						adapter.notifyDataSetChanged();

						if (showImages)
						{
							((TextView)view).setText("Hide images");
						}
						else
						{
							((TextView)view).setText("Show images");
						}
					}
				});
			}
		});

		return alertDialog;
	}
}
