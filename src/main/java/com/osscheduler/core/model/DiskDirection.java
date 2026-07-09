package com.osscheduler.core.model;

/**
 * The direction the disk head is moving in when a run begins.
 *
 * <p>{@link #UP} means the head moves towards larger cylinder numbers first,
 * {@link #DOWN} means it moves towards zero first. Some algorithms (SCAN,
 * C-SCAN, LOOK, C-LOOK) behave differently depending on this choice.</p>
 */
public enum DiskDirection {
    UP,
    DOWN
}
