package me.anon.grow;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;

import com.squareup.moshi.Types;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import me.anon.lib.helper.EncryptionHelper;
import me.anon.lib.helper.MoshiHelper;
import me.anon.lib.manager.PlantManager;
import me.anon.model.Plant;

/**
 * Class used for other applications to request data from this application
 */
public class RequestActivity extends Activity
{
	@Override protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getIntent() != null)
		{
			try
			{
				String callingPackage = getCallingPackage();
				CharSequence applicationLabel = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(callingPackage, 0));
				Drawable applicationIcon = getPackageManager().getApplicationIcon(getPackageManager().getApplicationInfo(callingPackage, 0));

				new AlertDialog.Builder(this)
					.setIcon(applicationIcon)
					.setMessage(Html.fromHtml("<b>" + applicationLabel + "</b> has requested a copy of the plant data"))
					.setPositiveButton("Allow", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							String plantListData = MoshiHelper.toJson(PlantManager.getInstance().getPlants(), Types.newParameterizedType(ArrayList.class, Plant.class));

							if (MainApplication.isEncrypted())
							{
								plantListData = Base64.encodeToString(EncryptionHelper.encrypt(MainApplication.getKey(), plantListData), Base64.NO_WRAP);
							}

							Intent ret = new Intent();
							ret.putExtra("me.anon.grow.PLANT_LIST", plantListData);
							ret.putExtra("me.anon.grow.ENCRYPTED", MainApplication.isEncrypted());

							setResult(RESULT_OK, ret);
							finish();
						}
					})
					.setNegativeButton("Deny", new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							finish();
						}
					})
					.show();
			}
			catch (PackageManager.NameNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
