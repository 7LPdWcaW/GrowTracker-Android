package me.anon.grow.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import lombok.Setter;
import me.anon.grow.R;
import me.anon.lib.Views;
import me.anon.model.Nutrient;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Views.Injectable
public class AddNutrientDialogFragment extends DialogFragment
{
	public interface OnAddNutrientListener
	{
		public void onNutrientSelected(Nutrient nutrient);
	}

	private Nutrient nutrient;
	@Views.InjectView(R.id.n) private TextView n;
	@Views.InjectView(R.id.p) private TextView p;
	@Views.InjectView(R.id.k) private TextView k;
	@Views.InjectView(R.id.ca) private TextView ca;
	@Views.InjectView(R.id.s) private TextView s;
	@Views.InjectView(R.id.mg) private TextView mg;
	@Setter private OnAddNutrientListener onAddNutrientListener;

	@SuppressLint("ValidFragment")
	public AddNutrientDialogFragment(Nutrient nutrient)
	{
		this.nutrient = nutrient;
	}

	public AddNutrientDialogFragment()
	{
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = getActivity().getLayoutInflater().inflate(R.layout.nutrient_dialog_view, null, false);
		Views.inject(this, view);

		if (nutrient != null)
		{
			n.setText(String.valueOf(nutrient.getNpc()));
			p.setText(String.valueOf(nutrient.getPpc()));
			k.setText(String.valueOf(nutrient.getKpc()));
			s.setText(String.valueOf(nutrient.getSpc()));
			ca.setText(String.valueOf(nutrient.getCapc()));
			mg.setText(String.valueOf(nutrient.getMgpc()));
		}

		return new AlertDialog.Builder(getActivity())
			.setTitle("Nutrient")
			.setView(view)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					Nutrient nutrient = new Nutrient();

					double npc = Double.valueOf(TextUtils.isEmpty(n.getText()) ? "0.0" : n.getText().toString());
					double ppc = Double.valueOf(TextUtils.isEmpty(p.getText()) ? "0.0" : p.getText().toString());
					double kpc = Double.valueOf(TextUtils.isEmpty(k.getText()) ? "0.0" : k.getText().toString());
					double spc = Double.valueOf(TextUtils.isEmpty(s.getText()) ? "0.0" : s.getText().toString());
					double capc = Double.valueOf(TextUtils.isEmpty(ca.getText()) ? "0.0" : ca.getText().toString());
					double mgpc = Double.valueOf(TextUtils.isEmpty(mg.getText()) ? "0.0" : mg.getText().toString());

					nutrient.setNpc(npc);
					nutrient.setPpc(ppc);
					nutrient.setKpc(kpc);
					nutrient.setSpc(spc);
					nutrient.setCapc(capc);
					nutrient.setMgpc(mgpc);

					if (onAddNutrientListener != null)
					{
						onAddNutrientListener.onNutrientSelected(nutrient);
					}
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.dismiss();
				}
			}).create();
	}
}
