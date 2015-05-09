package me.anon.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class Nutrient
{
	private double npc; // nitrogen
	private double ppc; // phosphorus
	private double kpc; // potassium
	private double capc; // calcium
	private double spc; // sulfur
	private double mgpc; // magnesium
}
