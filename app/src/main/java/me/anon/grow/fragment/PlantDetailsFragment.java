package me.anon.grow.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;
import com.kenny.snackbar.SnackBar;
import com.kenny.snackbar.SnackBarListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import me.anon.controller.provider.PlantWidgetProvider;
import me.anon.grow.AddWateringActivity;
import me.anon.grow.BuildConfig;
import me.anon.grow.EditWateringActivity;
import me.anon.grow.EventsActivity;
import me.anon.grow.MainApplication;
import me.anon.grow.PlantDetailsActivity;
import me.anon.grow.R;
import me.anon.grow.StatisticsActivity;
import me.anon.grow.ViewPhotosActivity;
import me.anon.lib.DateRenderer;
import me.anon.lib.ExportCallback;
import me.anon.lib.Views;
import me.anon.lib.helper.AddonHelper;
import me.anon.lib.helper.ExportHelper;
import me.anon.lib.helper.FabAnimator;
import me.anon.lib.helper.PermissionHelper;
import me.anon.lib.manager.GardenManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.task.AsyncCallback;
import me.anon.lib.task.EncryptTask;
import me.anon.model.EmptyAction;
import me.anon.model.NoteAction;
import me.anon.model.Plant;
import me.anon.model.PlantMedium;
import me.anon.model.PlantStage;
import me.anon.model.StageChange;
import me.anon.model.Water;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class PlantDetailsFragment extends Fragment
{
	@Views.InjectView(R.id.action_container) private View actionContainer;
	@Views.InjectView(R.id.link_container) private View linkContainer;

	@Views.InjectView(R.id.plant_name) private TextView name;
	@Views.InjectView(R.id.plant_strain) private TextView strain;
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

	private int plantIndex = -1;
	private int gardenIndex = -1;
	private Plant plant;

	/**
	 * @param plantIndex If -1, assume new plant
	 * @return Instantiated details fragment
	 */
	public static PlantDetailsFragment newInstance(int plantIndex, int gardenIndex)
	{
		Bundle args = new Bundle();
		args.putInt("plant_index", plantIndex);
		args.putInt("garden_index", gardenIndex);

		PlantDetailsFragment fragment = new PlantDetailsFragment();
		fragment.setArguments(args);

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

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null)
		{
			plantIndex = getArguments().getInt("plant_index", -1);
			gardenIndex = getArguments().getInt("garden_index", -1);

			if (plantIndex > -1)
			{
				plant = PlantManager.getInstance().getPlants().get(plantIndex);
				getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			}
		}

		if (plant == null)
		{
			plant = new Plant();
			getActivity().setTitle("Add new plant");

			plant.getActions().add(new StageChange(PlantStage.PLANTED));
			lastFeeding.setVisibility(View.GONE);
		}
		else
		{
			getActivity().setTitle("Plant details");

			actionContainer.setVisibility(View.VISIBLE);
			linkContainer.setVisibility(View.VISIBLE);

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
			lastFeeding.setCardBackgroundColor(0x9ABBDEFB);

			lastFeedingSummary.setText(Html.fromHtml(lastWater.getSummary(getActivity())));

			DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
			Date actionDate = new Date(lastWater.getDate());
			lastFeedingFullDate.setText(dateFormat.format(actionDate) + " " + timeFormat.format(actionDate));
			lastFeedingDate.setText(Html.fromHtml("<b>" + new DateRenderer().timeAgo(lastWater.getDate()).formattedDate + "</b> ago"));

			final Water finalLastWater = lastWater;
			duplicateFeeding.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					Kryo kryo = new Kryo();
					Water action = kryo.copy(finalLastWater);

					action.setDate(System.currentTimeMillis());
					PlantManager.getInstance().getPlants().get(plantIndex).getActions().add(action);
					PlantManager.getInstance().save();

					Intent editWater = new Intent(v.getContext(), EditWateringActivity.class);
					editWater.putExtra("plant_index", plantIndex);
					editWater.putExtra("action_index", PlantManager.getInstance().getPlants().get(plantIndex).getActions().size() - 1);
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
		clone.setChecked(plant.isClone());

		dateContainer.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				final DateDialogFragment fragment = new DateDialogFragment(plant.getPlantDate());
				fragment.setOnDateSelected(new DateDialogFragment.OnDateSelectedListener()
				{
					@Override public void onDateSelected(Calendar newDate)
					{
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

	@Views.OnClick public void onFeedingClick(final View view)
	{
		Intent feeding = new Intent(view.getContext(), AddWateringActivity.class);
		feeding.putExtra("plant_index", new int[]{plantIndex});
		startActivityForResult(feeding, 2);
	}

	@Views.OnClick public void onNoteClick(final View view)
	{
		NoteDialogFragment dialogFragment = new NoteDialogFragment();
		dialogFragment.setOnDialogConfirmed(new NoteDialogFragment.OnDialogConfirmed()
		{
			@Override public void onDialogConfirmed(String notes)
			{
				final NoteAction action = new NoteAction(notes);

				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plantIndex, plant);

				SnackBar.show(getActivity(), "Note added", "undo", new SnackBarListener()
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

					@Override public void onSnackBarAction(Object o)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plantIndex, plant);
					}
				});
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == 1 && (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
		{
			onPhotoClick(null);
		}
	}

	@Views.OnClick public void onPhotoClick(final View view)
	{
		if (!PermissionHelper.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
		{
			PermissionHelper.doPermissionCheck(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1, "Access to external storage is needed to store photos. No other data is consumed by this app");
			return;
		}

		String[] choices = {"From camera", "From gallery"};

		new AlertDialog.Builder(getActivity())
			.setTitle("Select an option")
			.setItems(choices, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialogInterface, int index)
				{
					if (index == 0)
					{
						Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

						File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/" + plant.getId() + "/");
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

						startActivityForResult(Intent.createChooser(intent, "Select picture"), 3);
					}
				}
			})
			.show();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == 1) // take image from camera
		{
			if (resultCode == Activity.RESULT_CANCELED)
			{
				new File(plant.getImages().get(plant.getImages().size() - 1)).delete();
				plant.getImages().remove(plant.getImages().size() - 1);
			}

			PlantManager.getInstance().upsert(plantIndex, plant);
			PlantWidgetProvider.triggerUpdateAll(getActivity());
			AddonHelper.broadcastImage(getActivity(), plant.getImages().get(plant.getImages().size() - 1), false);
		}
		else if (requestCode == 2)
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				SnackBar.show(getActivity(), "Watering added", "Apply to another plant", new SnackBarListener()
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

					@Override public void onSnackBarAction(Object o)
					{
						final ArrayList<Plant> sortedPlants = PlantManager.getInstance().getSortedPlantList(null);
						CharSequence[] plants = new CharSequence[sortedPlants.size()];
						for (int index = 0; index < plants.length; index++)
						{
							plants[index] = sortedPlants.get(index).getName();
						}

						new AlertDialog.Builder(getActivity())
							.setTitle("Select plant")
							.setItems(plants, new DialogInterface.OnClickListener()
							{
								@Override public void onClick(DialogInterface dialog, int which)
								{
									final int originalIndex = PlantManager.getInstance().getPlants().indexOf(sortedPlants.get(which));

									Water water = (Water)plant.getActions().get(plant.getActions().size() - 1);
									Water copy = new Kryo().copy(water);
									PlantManager.getInstance().getPlants().get(originalIndex).getActions().add(copy);

									Intent edit = new Intent(getActivity(), EditWateringActivity.class);
									edit.putExtra("plant_index", originalIndex);
									edit.putExtra("action_index", PlantManager.getInstance().getPlants().get(originalIndex).getActions().indexOf(copy));
									startActivityForResult(edit, 2);
								}
							})
							.show();
					}
				});
			}
		}
		else if (requestCode == 3) // choose image from gallery
		{
			if (resultCode != Activity.RESULT_CANCELED)
			{
				if (data != null && data.getData() != null)
				{
					Uri selectedUri = data.getData();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
					{
						getActivity().grantUriPermission(getActivity().getPackageName(), selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

						final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION);
						getActivity().getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
					}

					File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/" + plant.getId() + "/");
					path.mkdirs();

					try
					{
						new File(path, ".nomedia").createNewFile();
					}
					catch (IOException e){}

					File out = new File(path, System.currentTimeMillis() + ".jpg");

					copyImage(selectedUri, out);

					if (out.exists() && out.length() > 0)
					{
						plant.getImages().add(out.getAbsolutePath());
						PlantManager.getInstance().upsert(plantIndex, plant);
						AddonHelper.broadcastImage(getActivity(), out.getAbsolutePath(), false);
					}
					else
					{
						out.delete();
					}
				}
			}
		}
		else if (requestCode == 4)
		{
			setLastFeeding();
		}

		// both photo options
		if ((requestCode == 1 || requestCode == 3) && resultCode != Activity.RESULT_CANCELED)
		{
			if (getActivity() != null)
			{
				if (MainApplication.isEncrypted())
				{
					ArrayList<String> image = new ArrayList<>();
					image.add(plant.getImages().get(plant.getImages().size() - 1));
					new EncryptTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
				}

				SnackBar.show(getActivity(), "Image added", "Take another", new SnackBarListener()
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

					@Override public void onSnackBarAction(Object o)
					{
						onPhotoClick(null);
					}
				});
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void copyImage(Uri imageUri, File newLocation)
	{
		try
		{
			if (imageUri.getScheme().startsWith("content"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(imageUri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
				InputStream streamIn = new BufferedInputStream(new FileInputStream(fileDescriptor), 524288);
				OutputStream streamOut = new BufferedOutputStream(new FileOutputStream(newLocation), 524288);

				int len = 0;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
			else if (imageUri.getScheme().startsWith("file"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				String image = imageUri.getPath();

				InputStream streamIn = new BufferedInputStream(new FileInputStream(new File(image)), 524288);
				OutputStream streamOut = new BufferedOutputStream(new FileOutputStream(newLocation), 524288);

				int len = 0;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		if (plantIndex > -1)
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
				.setTitle("Are you sure?")
				.setMessage(Html.fromHtml("You are about to delete <b>" + plant.getName() + "</b> and all of the images associated with it, are you sure? This can not be undone."))
				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						final ProgressDialog progress = new ProgressDialog(getActivity());
						progress.setMessage("Deleting plant...");
						progress.setCancelable(false);
						progress.show();

						final Plant toDelete = PlantManager.getInstance().getPlants().get(plantIndex);
						PlantManager.getInstance().deletePlant(plantIndex, new AsyncCallback()
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
				.setNegativeButton("No", null)
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

			SnackBar.show(getActivity(), "Plant duplicated", "open", new SnackBarListener()
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

					@Override public void onSnackBarAction(Object o)
					{
						Intent plantDetails = new Intent(getActivity(), PlantDetailsActivity.class);
						plantDetails.putExtra("plant_index", PlantManager.getInstance().getPlants().size() - 1);
						startActivity(plantDetails);
					}
				});
		}
		else if (item.getItemId() == R.id.export)
		{
			Toast.makeText(getActivity(), "Exporting grow log...", Toast.LENGTH_SHORT).show();
			NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

			if (Build.VERSION.SDK_INT >= 26)
			{
				NotificationChannel channel = new NotificationChannel("export", "Export status", NotificationManager.IMPORTANCE_DEFAULT);
				notificationManager.createNotificationChannel(channel);
			}

			notificationManager.cancel(plantIndex);

			Notification exportNotification = new NotificationCompat.Builder(getActivity(), "export")
				.setContentText("Exporting grow log for " + plant.getName())
				.setContentTitle("Exporting")
				.setContentIntent(PendingIntent.getActivity(getActivity(), 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
				.setTicker("Exporting grow log for " + plant.getName())
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setSmallIcon(R.drawable.ic_stat_name)
				.build();

			notificationManager.notify(plantIndex, exportNotification);

			ExportHelper.exportPlant(getActivity(), plant, new ExportCallback()
			{
				@Override public void onCallback(Context context, File file)
				{
					if (file != null && file.exists() && getActivity() != null)
					{
						NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager.cancel(plantIndex);

						Intent openIntent = new Intent(Intent.ACTION_VIEW);
						Uri apkURI = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file);
						openIntent.setDataAndType(apkURI, "application/zip");
						openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

						Notification finishNotification = new NotificationCompat.Builder(getActivity(), "export")
							.setContentText("Exported " + plant.getName() + " to " + file.getAbsolutePath())
							.setTicker("Export of " + plant.getName() + " complete")
							.setContentTitle("Export Complete")
							.setContentIntent(PendingIntent.getActivity(getActivity(), 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT))
							.setSmallIcon(R.drawable.ic_stat_done)
							.setPriority(NotificationCompat.PRIORITY_HIGH)
							.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
							.setAutoCancel(true)
							.build();
						notificationManager.notify(plantIndex, finishNotification);

						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
						{
							new MediaScannerWrapper(getActivity(), file.getAbsolutePath(), "application/zip").scan();
						}
						else
						{
							getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
						}
					}
				}
			});

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Views.OnClick public void onActionClick(final View view)
	{
		ActionDialogFragment dialogFragment = new ActionDialogFragment();
		dialogFragment.setOnActionSelected(new ActionDialogFragment.OnActionSelected()
		{
			@Override public void onActionSelected(final EmptyAction action)
			{
				plant.getActions().add(action);
				PlantManager.getInstance().upsert(plantIndex, plant);

				SnackBar.show(getActivity(), action.getAction().getPrintString() + " added", "undo", new SnackBarListener()
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

					@Override public void onSnackBarAction(Object o)
					{
						plant.getActions().remove(action);
						PlantManager.getInstance().upsert(plantIndex, plant);
					}
				});
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onViewStatisticsClick(View view)
	{
		Intent stats = new Intent(getActivity(), StatisticsActivity.class);
		stats.putExtra("plant_index", plantIndex);
		startActivity(stats);
	}

	@Views.OnClick public void onViewHistoryClick(View view)
	{
		Intent events = new Intent(getActivity(), EventsActivity.class);
		events.putExtra("plant_index", plantIndex);
		startActivity(events);
	}

	@Views.OnClick public void onViewPhotosClick(View view)
	{
		Intent photos = new Intent(getActivity(), ViewPhotosActivity.class);
		photos.putExtra("plant_index", plantIndex);
		startActivity(photos);
	}

	@Views.OnClick public void onPlantStageContainerClick(final View view)
	{
		StageDialogFragment dialogFragment = StageDialogFragment.newInstance();
		dialogFragment.setOnStageUpdated(new StageDialogFragment.OnStageUpdated()
		{
			@Override public void onStageUpdated(final StageChange action)
			{
				stage.setText(action.getNewStage().getPrintString());

				if (plantIndex > -1)
				{
					plant.getActions().add(action);
					PlantManager.getInstance().upsert(plantIndex, plant);

					SnackBar.show(getActivity(), "Stage updated", "undo", new SnackBarListener()
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

						@Override public void onSnackBarAction(Object o)
						{
							if (plantIndex > -1)
							{
								plant.getActions().remove(action);
								PlantManager.getInstance().upsert(plantIndex, plant);
							}

							if (plant.getStage() != null)
							{
								stage.setText(plant.getStage().getPrintString());
							}
						}
					});
				}
			}
		});
		dialogFragment.show(getFragmentManager(), null);
	}

	@Views.OnClick public void onPlantMediumContainerClick(final View view)
	{
		String[] mediums = PlantMedium.names();

		new AlertDialog.Builder(view.getContext())
			.setTitle("Medium")
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
		name.setError(null);
		strain.setError(null);

		if (!TextUtils.isEmpty(name.getText()))
		{
			plant.setName(name.getText().toString().trim());
		}
		else
		{
			name.setError("Name can not be empty");
			return;
		}

		if (!TextUtils.isEmpty(strain.getText()))
		{
			plant.setStrain(strain.getText().toString().trim());
		}
		else
		{
			strain.setError("strain can not be empty");
			return;
		}

		plant.setMediumDetails(mediumDetails.getText().toString());

		PlantStage newStage = PlantStage.valueOf(stage.getText().toString().toUpperCase(Locale.ENGLISH));
		if (plant.getStage() != newStage || (plantIndex < 0 && newStage == PlantStage.GERMINATION))
		{
			plant.getActions().add(new StageChange(newStage));
		}

		if (plantIndex < 0)
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			SharedPreferences.Editor edit = prefs.edit();

			int plantsSize = PlantManager.getInstance().getPlants().size();

			for (int index = 0; index < plantsSize; index++)
			{
				int currentPos = prefs.getInt(String.valueOf(index), 0);

				edit.putInt(String.valueOf(index), currentPos + 1);
			}

			edit.putInt(String.valueOf(plantsSize + 1), 0);
			edit.apply();
		}

		plant.setClone(clone.isChecked());
		PlantManager.getInstance().upsert(plantIndex, plant);

		if (gardenIndex != -1)
		{
			if (!GardenManager.getInstance().getGardens().get(gardenIndex).getPlantIds().contains(plant.getId()))
			{
				GardenManager.getInstance().getGardens().get(gardenIndex).getPlantIds().add(plant.getId());
				GardenManager.getInstance().save();
			}
		}

		PlantWidgetProvider.triggerUpdateAll(getActivity());
		getActivity().finish();
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
