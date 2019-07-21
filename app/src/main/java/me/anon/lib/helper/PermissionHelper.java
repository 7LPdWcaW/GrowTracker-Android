package me.anon.lib.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.PermissionChecker;

public class PermissionHelper
{
	@TargetApi(Build.VERSION_CODES.M)
	public static boolean doPermissionCheck(final Fragment fragment, final String permission, final int requestCode, String dialogMessage)
	{
		if (PermissionChecker.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED)
		{
			if (fragment.shouldShowRequestPermissionRationale(permission) && dialogMessage != null)
			{
				new AlertDialog.Builder(fragment.getActivity())
					.setMessage(dialogMessage)
					.setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							fragment.requestPermissions(new String[]{permission}, requestCode);
						}
					})
					.setNegativeButton("Cancel", null)
					.show();

				return false;
			}

			fragment.requestPermissions(new String[]{permission}, requestCode);
			return false;
		}

		return true;
	}

	@TargetApi(Build.VERSION_CODES.M)
	public static boolean doPermissionCheck(final Activity activity, final String permission, final int requestCode, String dialogMessage)
	{
		if (PermissionChecker.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
		{
			if (activity.shouldShowRequestPermissionRationale(permission) && dialogMessage != null)
			{
				new AlertDialog.Builder(activity)
					.setMessage(dialogMessage)
					.setPositiveButton("OK", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							activity.requestPermissions(new String[]{permission}, requestCode);
						}
					})
					.setNegativeButton("Cancel", null)
					.show();

				return false;
			}

			activity.requestPermissions(new String[]{permission}, requestCode);
			return false;
		}

		return true;
	}

	public static boolean hasPermission(Context context, String permission)
	{
		return Build.VERSION.SDK_INT < 23 || PermissionChecker.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
	}
}
