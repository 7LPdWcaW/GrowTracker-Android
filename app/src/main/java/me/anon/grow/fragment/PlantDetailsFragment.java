package me.anon.grow.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.ActionsActivity;
import me.anon.grow.AddWateringActivity;
import me.anon.grow.BuildConfig;
import me.anon.grow.EditWateringActivity;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.grow.StatisticsActivity;
import me.anon.grow.ViewPhotosActivity;
import me.anon.lib.DateRenderer;
import me.anon.lib.SnackBar;
import me.anon.lib.SnackBarListener;
import me.anon.lib.Views;
import me.anon.lib.export.ExportHelper;
import me.anon.lib.export.ExportProcessor;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.helper.PermissionHelper;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.AsyncCallback;
import me.anon.lib.task.EncryptTask;
import me.anon.lib.task.ImportTask;
import me.anon.model.Action;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

@Views.Injectable
public class PlantDetailsFragment extends Fragment
{
	@Views.InjectView(R.id.link_container) private View linkContainer;

	@Views.InjectView(R.id.plant_name) private TextView name;
	@Views.InjectView(R.id.plant_strain) private AutoCompleteTextView strain;
	@Views.InjectView(R.id.plant_stage) private TextView stage;
	@Views.InjectView(R.id.plant_medium) private TextView medium;
	@Views.InjectView(R.id.plant_medium_details) private EditText mediumDetails;
	@Views.InjectView(R.id.plant_date) private TextView date;
	@Views.InjectView(R.id.plant_date_container) private View dateContainer;
	@Views.InjectView(R.id.from_clone) private CheckBox clone;

	@Views.InjectView(R.id.last_feeding) private CardView lastFeeding;
	@Views.InjectView(R.id.last_feeding_date) private TextView lastFeedingDate;
	@Views.InjectView(R.id.last_feeding_full_date) private TextView lastFeedingFullDate;
	@Views.InjectView(R.id.last_feeding_name) private TextView lastFeedingName;
	@Views.InjectView(R.id.last_feeding_summary) private TextView lastFeedingSummary;
	@Views.InjectView(R.id.duplicate_feeding) private Button duplicateFeeding;

	private int gardenIndex = -1;
	private Plant plant = null;
	private boolean forwardIntent = false;
	private boolean newPlant = false;

	public static final int ACTIVITY_REQUEST_PHOTO_CAMERA = 1;
	public static final int ACTIVITY_REQUEST_FEEDING = 2;
	public static final int ACTIVITY_REQUEST_PHOTO_GALLERY = 3;
	public static final int ACTIVITY_REQUEST_LAST_WATER = 4;

	/**
	 * @return Instantiated details fragment
	 */
	public static PlantDetailsFragment newInstance(Bundle bundle)
	{
		PlantDetailsFragment fragment = new PlantDetailsFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.plant_details_view, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		outState.putInt("garden_index", gardenIndex);
		outState.putParcelable("plant", plant);
		outState.putBoolean("new_plant", newPlant);
		super.onSaveInstanceState(outState);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null)
		{
			gardenIndex = getArguments().getInt("garden_index", gardenIndex);
			plant = getArguments().getParcelable("plant");
		}

		if (savedInstanceState != null)
		{
			gardenIndex = savedInstanceState.getInt("garden_index", gardenIndex);
			plant = savedInstanceState.getParcelable("plant");
			newPlant = savedInstanceState.getBoolean("new_plant", newPlant);
		}

		if (getActivity().getIntent().getExtras() != null && getActivity().getIntent().getExtras().containsKey("forward"))
		{
			Bundle extras = getActivity().getIntent().getExtras();
			String forward = extras.getString("forward", "");
			forwardIntent = !TextUtils.isEmpty(forward);

			if ("feed".equals(forward))
			{
				onFeedingClick();
			}
			else if ("action".equals(forward))
			{
				onActionClick();
			}
			else if ("note".equals(forward))
			{
				onNoteClick();
			}
			else if ("photo".equals(forward))
			{
				onPhotoClick();
			}
			else if ("photos".equals(forward))
			{
				onViewPhotosClick();
			}
			else if ("history".equals(forward))
			{
				onViewHistoryClick();
			}
			else if ("statistics".equals(forward))
			{
				onViewStatisticsClick();
			}
		}

		if (plant == null)
		{
			newPlant = true;
			plant = new Plant();
			getActivity().setTitle(getString(R.string.new_plant_title));

			plant.getActions().add(new StageChange());
			lastFeeding.setVisibility(View.GONE);
		}
		else
		{
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			getActivity().setTitle(getString(R.string.plant_details_title));

			((PlantDetailsActivity)getActivity()).getToolbarLayout().addView(LayoutInflater.from(getActivity()).inflate(R.layout.action_buttons_stub, ((PlantDetailsActivity)getActivity()).getToolbarLayout(), false));
			Views.inject(this, ((PlantDetailsActivity)getActivity()).getToolbarLayout());
			//linkContainer.setVisibility(View.VISIBLE);

			name.setText(plant.getName());

			if (plant.getMediumDetails() != null)
			{
				mediumDetails.setText(plant.getMediumDetails());
			}

			if (plant.getMedium() != null)
			{
				medium.setText(plant.getMedium().getPrintString());
			}

			setLastFeeding();
		}

		setUi();
	}

	@Override public void onResume()
	{
		super.onResume();

		// Always re-set stage incase order was changed in event list
		if (plant != null && plant.getStage() != null)
		{
			stage.setTag(plant.getStage());
			stage.setText(plant.getStage().getPrintString());
		}
	}

	private void setLastFeeding()
	{
		Water lastWater = null;
		for (int index = plant.getActions().size() - 1; index >= 0; index--)
		{
			if (plant.getActions().get(index) instanceof Water)
			{
				lastWater = (Water)plant.getActions().get(index);
				break;
			}
		}

		lastFeeding.setVisibility(View.GONE);
		if (lastWater != null)
		{
			lastFeeding.setVisibility(View.VISIBLE);

			String summary = lastWater.getSummary(getActivity());
			if (!TextUtils.isEmpty(lastWater.getNotes()))
			{
				summary += "<br /><br />";
				summary += lastWater.getNotes();
			}

			lastFeedingSummary.setText(Html.fromHtml(summary));

			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
			Date actionDate = new Date(lastWater.getDate());
			lastFeedingFullDate.setText(dateFormat.format(actionDate) + " " + timeFormat.format(actionDate));
			lastFeedingDate.setText(Html.fromHtml("<b>" + new DateRenderer(getActivity()).timeAgo(lastWater.getDate()).formattedDate + "</b> ago"));

			final Water finalLastWater = lastWater;
			duplicateFeeding.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					Kryo kryo = new Kryo();
					Water action = kryo.copy(finalLastWater);

					action.setDate(System.currentTimeMillis());
					plant.getActions().add(action);
					PlantManager.getInstance().upsert(plant);

					Intent editWater = new Intent(v.getContext(), EditWateringActivity.class);
					editWater.putExtra("plant_index", PlantManager.getInstance().indexOf(plant));
					editWater.putExtra("action_index", plant.getActions().size() - 1);
					editWater.putExtra("new_water", true);
					startActivityForResult(editWater, 4);
				}
			});
		}
	}

	private void setUi()
	{
		final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());

		String dateStr = dateFormat.format(new Date(plant.getPlantDate())) + " " + timeFormat.format(new Date(plant.getPlantDate()));
		date.setText(dateStr);
		clone.setChecked(plant.getClone());

		Set<String> strains = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		for (Plant plant : PlantManager.getInstance().getPlants())
		{
			if (!TextUtils.isEmpty(plant.getStrain()))
			{
				strains.add(plant.getStrain());
			}
		}

		strain.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, strains.toArray(new String[strains.size()])));

		dateContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(plant.getPlantDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar newDate)
					{
						for (Action action : plant.getActions())
						{
							if (action instanceof StageChange && ((StageChange)action).getNewStage() == PlantStage.PLANTED)
							{
								action.setDate(newDate.getTimeInMillis());
							}
						}

						plant.setPlantDate(newDate.getTimeInMillis());
						String dateStr = dateFormat.format(new Date(plant.getPlantDate())) + " " + timeFormat.format(new Date(plant.getPlantDate()));
						date.setText(dateStr);

						onCancelled();
					}

					@Override public void onCancelled()
					{
						getFragmentManager().beginTransaction().remove(fragment).commit();
					}
				});
				getFragmentManager().beginTransaction().add(fragment, "date").commit();
			}
		});

		strain.setText(plant.getStrain());
	}

	@Views.OnClick public void onFeedingClick()
	{
		Intent feeding = new Intent(getActivity(), AddWateringActivity.class);
		feeding.putExtra("plant_index", new int[]{PlantManager.getInstance().indexOf(plant)});
		startActivityForResult(feeding, ACTIVITY_REQUEST_FEEDING);
	}

	@Views.OnClick public void onNoteClick()
	{
		NoteDialogFragment dialogFragment = new NoteDialogFragment();
		dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
		{
			@Override public void onDialogConfirmed(String notes)
			{
				final NoteAction action = new NoteAction(System.currentTimeMillis(), notes);

				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plant);

				SnackBar.show(getActivity(), R.string.snackbar_note_add, R.string.snackbar_undo, new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							PlantWidgetProvider.triggerUpdateAll(getView().getContext());
						}
					}

					@Override public void onSnackBarAction(View v)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plant);
					}
				});
			}
		});
		dialogFragment.onCancelListener = new DialogInterface.OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialogInterface)
			{
				if (forwardIntent) getActivity().finish();
			}
		};
		dialogFragment.show(getFragmentManager(), null);
	}

	@Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == 1 && (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
		{
			onPhotoClick();
		}
	}

	@Views.OnClick public void onPhotoClick()
	{
		if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.CAMERA))
		{
			PermissionHelper.doPermissionCheck(this, Manifest.permission.CAMERA, 1, getString(R.string.camera_permission_summary));
			return;
		}

		if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			PermissionHelper.doPermissionCheck(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, getString(R.string.permission_summary));
			return;
		}

		String[] choices = {getString(R.string.photo_option_camera), getString(R.string.photo_option_gallery)};

		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.dialog_option_title)
			.setItems(choices, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialogInterface, int index)
				{
					if (index == 0)
					{
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

						File path = new File(FileManager.IMAGE_PATH + plant.getId() + "/");
						path.mkdirs();

						try
						{
							new File(path, ".nomedia").createNewFile();
						}
						catch (IOException e){}

						File out = new File(path, System.currentTimeMillis() + ".jpg");
						Uri photoURI = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", out);

						plant.getImages().add(out.getAbsolutePath());

						intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
						startActivityForResult(intent, 1);
					}
					else
					{
						if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
						{
							PermissionHelper.doPermissionCheck(PlantDetailsFragment.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, "Need permission");
							return;
						}

						Intent intent;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						{
							intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
							intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
							intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
						}
						else
						{
							intent = new Intent(Intent.ACTION_GET_CONTENT);
						}

						intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						intent.setType("image/*");

						startActivityForResult(Intent.createChooser(intent, getString(R.string.dialog_select_picture_title)), 3);
					}
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override public void onCancel(DialogInterface dialogInterface)
				{
					if (forwardIntent) getActivity().finish();
				}
			})
			.show();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra("plant"))
		{
			plant = data.getParcelableExtra("plant");
			setLastFeeding();
			setUi();
		}

		if (requestCode == ACTIVITY_REQUEST_PHOTO_CAMERA) // take image from camera
		{
			if (resultCode == Activity.RESULT_CANCELED)
			{
				new File(plant.getImages().get(plant.getImages().size() - 1)).delete();
				plant.getImages().remove(plant.getImages().size() - 1);
			}
			else
			{
				PlantManager.getInstance().upsert(plant);
				PlantWidgetProvider.triggerUpdateAll(getActivity());
				finishPhotoIntent();
			}
		}
		else if (requestCode == ACTIVITY_REQUEST_FEEDING)
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				setLastFeeding();
				SnackBar.show(getActivity(), R.string.snackbar_watering_add, R.string.snackbar_action_apply, new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarAction(View v)
					{
						PlantSelectDialogFragment dialog = new PlantSelectDialogFragment(true);
//						dialog.setDisabled(plantIndex);
						dialog.setOnDialogActionListener(new PlantSelectDialogFragment.OnDialogActionListener()
						{
							@Override public void onDialogAccept(ArrayList<Integer> plantIndex, boolean showImage)
							{
								Water water = (Water)plant.getActions().get(plant.getActions().size() - 1);

								for (Integer index : plantIndex)
								{
									Water copy = new Kryo().copy(water);
									PlantManager.getInstance().getPlants().get(index).getActions().add(copy);
								}

								SnackBar.show(getActivity(), R.string.waterings_added, new SnackBarListener()
								{
									@Override public void onSnackBarStarted(Object o)
									{
										if (getView() != null)
										{
											FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
										}
									}

									@Override public void onSnackBarFinished(Object o)
									{
										if (getView() != null)
										{
											FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
										}
									}

									@Override public void onSnackBarAction(@NotNull View o)
									{
										if (getView() != null)
										{
											FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
										}
									}
								});
							}
						});
						dialog.show(getFragmentManager(), "plant-select");
					}
				});
			}
		}
		else if (requestCode == ACTIVITY_REQUEST_PHOTO_GALLERY) // choose image from gallery
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				if (data == null) return;

				ArrayList<Uri> images = new ArrayList<>();
				if (data.getData() != null)
				{
					images.add(data.getData());

					try
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						{
							getActivity().grantUriPermission(getActivity().getPackageName(), data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);

							final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
							getActivity().getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				if (data.getClipData() != null)
				{
					for (int index = 0; index < data.getClipData().getItemCount(); index++)
					{
						images.add(data.getClipData().getItemAt(index).getUri());
					}
				}
				images.removeAll(Collections.singleton(null));

				NotificationHelper.sendDataTaskNotification(getActivity(), getString(R.string.app_name), getString(R.string.import_progress_warning));
				new ImportTask(getActivity(), new AsyncCallback()
				{
					@Override public void callback()
					{
						if (getActivity() != null && !getActivity().isFinishing())
						{
							plant = PlantManager.getInstance().getPlant(plant.getId());
							finishPhotoIntent();
						}
					}
				}).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Pair<>(plant.getId(), images));
			}
		}
		else if (requestCode == ACTIVITY_REQUEST_LAST_WATER)
		{
			setLastFeeding();
		}

		// both photo options
		if ((requestCode == ACTIVITY_REQUEST_PHOTO_CAMERA || requestCode == ACTIVITY_REQUEST_PHOTO_GALLERY) && resultCode != Activity.RESULT_CANCELED)
		{
			if (getActivity() != null)
			{
				if (MainApplication.isEncrypted())
				{
					ArrayList<String> image = new ArrayList<>();
					image.add(plant.getImages().get(plant.getImages().size() - 1));
					new EncryptTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
				}

				SnackBar.show(getActivity(), R.string.snackbar_image_added, R.string.snackbar_action_take_another, new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							PlantWidgetProvider.triggerUpdateAll(getView().getContext());
						}
					}

					@Override public void onSnackBarAction(View v)
					{
						onPhotoClick();
					}
				});
			}
		}

		if (forwardIntent)
		{
			getActivity().finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void finishPhotoIntent()
	{
		Intent intent = new Intent();
		intent.putExtra("plant", plant);
		getActivity().setIntent(intent);
		getActivity().setResult(Activity.RESULT_OK, intent);

		PlantWidgetProvider.triggerUpdateAll(getView().getContext());

		if (getActivity() != null)
		{
			if (MainApplication.isEncrypted())
			{
				ArrayList<String> image = new ArrayList<>();
				image.add(plant.getImages().get(plant.getImages().size() - 1));
				new EncryptTask(getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
			}

			SnackBar.show(getActivity(), R.string.snackbar_image_added, R.string.snackbar_action_take_another, new SnackBarListener()
			{
				@Override public void onSnackBarStarted(Object o){}
				@Override public void onSnackBarFinished(Object o){}

				@Override public void onSnackBarAction(View v)
				{
					onPhotoClick();
				}
			});
		}
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		if (!newPlant)
		{
			inflater.inflate(R.menu.plant_menu, menu);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.delete)
		{
			new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm_title)
				.setMessage(Html.fromHtml(getString(R.string.delete_plant_message, plant.getName())))
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						final ProgressDialog progress = new ProgressDialog(getActivity());
						progress.setMessage(getString(R.string.delete_plant_progress));
						progress.setCancelable(false);
						progress.show();

						PlantManager.getInstance().deletePlant(plant, new AsyncCallback()
						{
							@Override public void callback()
							{
								PlantManager.getInstance().save(new AsyncCallback()
								{
									@Override public void callback()
									{
										if (progress != null)
										{
											progress.dismiss();
										}

										if (getActivity() != null)
										{
											getActivity().finish();
										}
									}
								}, true);
							}
						});
					}
				})
				.setNegativeButton(R.string.no, null)
				.show();

			return true;
		}
		else if (item.getItemId() == R.id.duplicate)
		{
			final Plant copy = new Kryo().copy(plant);
			copy.setId(UUID.randomUUID().toString());
			copy.getImages().clear();

			copy.setName(copy.getName() + " (copy)");
			PlantManager.getInstance().addPlant(copy);

			SnackBar.show(getActivity(), R.string.plant_duplicated, R.string.open, new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarAction(View v)
					{
						Intent plantDetails = new Intent(getActivity(), PlantDetailsActivity.class);
						plantDetails.putExtra("plant", PlantManager.getInstance().getPlants().get(PlantManager.getInstance().getPlants().size() - 1));
						startActivity(plantDetails);
					}
				});
		}
		else if (item.getItemId() == R.id.export)
		{
			new ExportDialogFragment(new Function2<Class<? extends ExportProcessor>, Boolean, Unit>()
			{
				@Override public Unit invoke(Class<? extends ExportProcessor> processor, Boolean includeImages)
				{
					Toast.makeText(getActivity(), R.string.export_progress, Toast.LENGTH_SHORT).show();
					new ExportHelper(getActivity(), processor, includeImages).exportPlants(new ArrayList<>(Arrays.asList(plant)));
					return null;
				}
			}).show(getFragmentManager(), "export_dialog");

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Views.OnClick public void onActionClick()
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(final EmptyAction action)
			{
				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plant);

				SnackBar.show(getActivity(), getString(R.string.action_added, getString(action.getAction().getPrintString())), getString(R.string.undo), new SnackBarListener()
				{
					@Override public void onSnackBarStarted(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateUp(getView().findViewById(R.id.fab_complete));
						}
					}

					@Override public void onSnackBarFinished(Object o)
					{
						if (getView() != null)
						{
							FabAnimator.animateDown(getView().findViewById(R.id.fab_complete));
							PlantWidgetProvider.triggerUpdateAll(getView().getContext());
						}
					}

					@Override public void onSnackBarAction(View v)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plant);
					}
				});
			}
		});
		dialogFragment.onCancelListener = new DialogInterface.OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialogInterface)
			{
				if (forwardIntent) getActivity().finish();
			}
		};
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onViewStatisticsClick()
	{
		Intent stats = new Intent(getActivity(), StatisticsActivity.class);
		stats.putExtra("plant", plant);
		startActivityForResult(stats, 5);
	}

	@Views.OnClick public void onViewHistoryClick()
	{
		Intent events = new Intent(getActivity(), ActionsActivity.class);
		events.putExtra("plant", plant);
		startActivityForResult(events, 5);
	}

	@Views.OnClick public void onViewPhotosClick()
	{
		Intent photos = new Intent(getActivity(), ViewPhotosActivity.class);
		photos.putExtra("plant", plant);
		startActivityForResult(photos, 5);
	}

	@Views.OnClick public void onPlantStageContainerClick()
	{
		StageDialogFragment dialogFragment = StageDialogFragment.newInstance();
		dialogFragment.setOnStageUpdated(new StageDialogFragment.OnStageUpdated()
		{
			@Override public void onStageUpdated(final StageChange action)
			{
				stage.setTag(action.getNewStage());
				stage.setText(action.getNewStage().getPrintString());

				if (plant != null)
				{
					plant.getActions().add(action);
					PlantManager.getInstance().upsert(plant);

					SnackBar.show(getActivity(), R.string.stage_updated, R.string.undo, new SnackBarListener()
					{
						@Override public void onSnackBarStarted(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateUp(getView().findViewById(R.id.fab_add));
							}
						}

						@Override public void onSnackBarFinished(Object o)
						{
							if (getView() != null)
							{
								FabAnimator.animateDown(getView().findViewById(R.id.fab_add));
							}
						}

						@Override public void onSnackBarAction(View v)
						{
							if (plant != null)
							{
								plant.getActions().remove(action);
								PlantManager.getInstance().upsert(plant);
							}

							PlantStage plantStage = plant.getStage();
							if (plantStage != null)
							{
								stage.setTag(plantStage);
								stage.setText(plantStage.getPrintString());
							}
						}
					});
				}
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onPlantMediumContainerClick()
	{
		String[] mediums = PlantMedium.Companion.names(getActivity());

		new AlertDialog.Builder(getActivity())
			.setTitle(R.string.plant_medium_label)
			.setItems(mediums, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					medium.setText(PlantMedium.values()[which].getPrintString());
					plant.setMedium(PlantMedium.values()[which]);
				}
			})
			.show();
	}

	public void save()
	{
		if (!TextUtils.isEmpty(name.getText()))
		{
			plant.setName(name.getText().toString().trim());
		}
		else
		{
			plant.setName(("Untitled " + (PlantManager.getInstance().getPlants().size() + 1)).trim());
		}

		if (!TextUtils.isEmpty(strain.getText()))
		{
			plant.setStrain(strain.getText().toString().trim());
		}

		plant.setMediumDetails(mediumDetails.getText().toString());

		PlantStage newStage = (PlantStage)stage.getTag();
		if (plant.getStage() != newStage || (plant == null && newStage == PlantStage.GERMINATION))
		{
			plant.getActions().add(new StageChange(newStage, System.currentTimeMillis(), null));
		}

		plant.setClone(clone.isChecked());
		//PlantManager.getInstance().upsert(plant);

		if (gardenIndex != -1)
		{
			if (!GardenManager.getInstance().getGardens().get(gardenIndex).getPlantIds().contains(plant.getId()))
			{
				GardenManager.getInstance().getGardens().get(gardenIndex).getPlantIds().add(plant.getId());
				GardenManager.getInstance().save();
			}
		}

		Intent intent = new Intent();
		intent.putExtra("plant", plant);
		getActivity().setIntent(intent);
		getActivity().setResult(Activity.RESULT_OK, intent);
//		getActivity().finish();
	}

	/**
	 * Media scanner class to tell the OS to pick up the images taken via the app in the gallery
	 * viewers
	 */
	public static class MediaScannerWrapper implements MediaScannerConnection.MediaScannerConnectionClient
	{
		private MediaScannerConnection mConnection;
		private String mPath;
		private String mMimeType;

		// filePath - where to scan;
		// mime type of media to scan i.e. "image/jpeg".
		// use "*/*" for any media
		public MediaScannerWrapper(Context ctx, String filePath, String mime)
		{
			mPath = filePath;
			mMimeType = mime;
			mConnection = new MediaScannerConnection(ctx, this);
		}

		// do the scanning
		public void scan()
		{
			mConnection.connect();
		}

		// start the scan when scanner is ready
		public void onMediaScannerConnected()
		{
			mConnection.scanFile(mPath, mMimeType);
		}

		public void onScanCompleted(String path, Uri uri)
		{
			try
			{
				mConnection.disconnect();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
