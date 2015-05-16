package me.anon.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public class Water extends Action
{
	private Long ppm;
	private Double ph;
	private Double runoff;
	private Integer amount;
}
