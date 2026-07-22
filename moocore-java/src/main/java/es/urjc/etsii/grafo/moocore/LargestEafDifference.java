// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

/** Pair of datasets with the largest EAF difference and its value. */
public record LargestEafDifference(int left, int right, double value) {}
