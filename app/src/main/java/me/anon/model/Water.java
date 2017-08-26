package me.anon.model;

import java.util.ArrayList;
import java.util.List;

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
public class Water extends Action
{
	private Double ppm;
	private Double ph;
	private Double runoff;
	private Double amount;
	private Double temp;
	private List<Additive> additives = new ArrayList<>();

	@Deprecated private Nutrient nutrient;
	@Deprecated private Double mlpl;
}
