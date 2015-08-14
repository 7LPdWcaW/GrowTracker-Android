package me.anon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
public class Nutrient
{
	private Double npc; // nitrogen
	private Double ppc; // phosphorus
	private Double kpc; // potassium
	private Double capc; // calcium
	private Double spc; // sulfur
	private Double mgpc; // magnesium
}
