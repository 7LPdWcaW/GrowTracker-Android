package me.anon.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Deprecated
@Getter @Setter
@Accessors(prefix = {"m", ""}, chain = true)
@NoArgsConstructor
public class Nutrient
{
	private Double npc; // nitrogen
	private Double ppc; // phosphorus
	private Double kpc; // potassium
	private Double capc; // calcium
	private Double spc; // sulfur
	private Double mgpc; // magnesium
}
