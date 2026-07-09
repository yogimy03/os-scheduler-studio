package com.osscheduler.core.page;

import com.osscheduler.core.model.PageResult;

import java.util.List;

/**
 * The common contract for every page replacement algorithm.
 *
 * <p>Memory has a fixed number of frames. As a program runs it references pages;
 * if the page is not already in a frame that is a "page fault" and the operating
 * system must load it, possibly throwing out another page to make room. Each
 * algorithm chooses <em>which</em> page to throw out differently, and that
 * choice decides how many faults happen.</p>
 */
public interface PageReplacementAlgorithm {

    /** The name shown in menus and chart titles. */
    String name();

    /**
     * Replay the reference string against the given number of frames.
     *
     * @param references the pages requested, in order
     * @param frameCount how many frames memory has
     * @return a step by step account plus the fault and hit totals
     */
    PageResult run(List<Integer> references, int frameCount);
}
