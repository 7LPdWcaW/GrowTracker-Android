package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Deprecated
public class Nutrient
{
	private Double npc; // nitrogen
	private Double ppc; // phosphorus
	private Double kpc; // potassium
	private Double capc; // calcium
	private Double spc; // sulfur
	private Double mgpc; // magnesium

	public Double getNpc()
	{
		return npc;
	}

	public void setNpc(Double npc)
	{
		this.npc = npc;
	}

	public Double getPpc()
	{
		return ppc;
	}

	public void setPpc(Double ppc)
	{
		this.ppc = ppc;
	}

	public Double getKpc()
	{
		return kpc;
	}

	public void setKpc(Double kpc)
	{
		this.kpc = kpc;
	}

	public Double getCapc()
	{
		return capc;
	}

	public void setCapc(Double capc)
	{
		this.capc = capc;
	}

	public Double getSpc()
	{
		return spc;
	}

	public void setSpc(Double spc)
	{
		this.spc = spc;
	}

	public Double getMgpc()
	{
		return mgpc;
	}

	public void setMgpc(Double mgpc)
	{
		this.mgpc = mgpc;
	}
}
