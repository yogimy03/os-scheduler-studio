package com.osscheduler.core.disk;

import com.osscheduler.core.model.DiskDirection;
import com.osscheduler.core.model.DiskResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the disk algorithms against the classic textbook example: the head
 * starts at cylinder 53 on a 200 cylinder disk with the request queue
 * 98, 183, 37, 122, 14, 124, 65, 67.
 */
class DiskSchedulerTest {

    private static final int HEAD = 53;
    private static final int SIZE = 200;
    private static final List<Integer> REQUESTS = List.of(98, 183, 37, 122, 14, 124, 65, 67);

    private static DiskResult run(DiskScheduler scheduler) {
        return scheduler.schedule(HEAD, REQUESTS, SIZE, DiskDirection.UP);
    }

    @Test
    void fcfsMovesInRequestOrder() {
        DiskResult result = run(new FcfsDiskScheduler());
        assertEquals(640, result.totalHeadMovement());
        assertEquals(HEAD, result.path().get(0));
    }

    @Test
    void sstfPicksNearestRequest() {
        assertEquals(236, run(new SstfDiskScheduler()).totalHeadMovement());
    }

    @Test
    void scanReachesTheEndThenReverses() {
        DiskResult result = run(new ScanDiskScheduler());
        assertEquals(331, result.totalHeadMovement());
        assertEquals(199, result.path().get(7)); // walks to the top end
    }

    @Test
    void cscanJumpsBackToTheStart() {
        assertEquals(382, run(new CscanDiskScheduler()).totalHeadMovement());
    }

    @Test
    void lookTurnsAtTheLastRequest() {
        DiskResult result = run(new LookDiskScheduler());
        assertEquals(299, result.totalHeadMovement());
        // Never travels past the largest request (183) to the physical end.
        assertEquals(183, result.path().stream().mapToInt(Integer::intValue).max().orElseThrow());
    }

    @Test
    void clookTurnsAtTheLastRequestAndWrapsToTheFirst() {
        assertEquals(322, run(new ClookDiskScheduler()).totalHeadMovement());
    }

    @Test
    void scanWorksDownwardToo() {
        // Head 50, requests below and above, sweeping down first to cylinder 0.
        DiskResult result = new ScanDiskScheduler().schedule(
                50, List.of(10, 20, 30, 70, 80, 90), 100, DiskDirection.DOWN);
        assertEquals(140, result.totalHeadMovement());
        assertEquals(List.of(50, 30, 20, 10, 0, 70, 80, 90), result.path());
    }

    @Test
    void scanReachesTheEndEvenWhenAllRequestsAreOnTheSweepSide() {
        // Head 50 sweeping up, every request above it: SCAN must still drive to
        // the physical end (199), unlike LOOK which would stop at 150.
        DiskResult scan = new ScanDiskScheduler().schedule(
                50, List.of(70, 90, 120, 150), 200, DiskDirection.UP);
        assertEquals(List.of(50, 70, 90, 120, 150, 199), scan.path());
        assertEquals(149, scan.totalHeadMovement());

        DiskResult look = new LookDiskScheduler().schedule(
                50, List.of(70, 90, 120, 150), 200, DiskDirection.UP);
        assertEquals(100, look.totalHeadMovement()); // LOOK turns at the last request
    }

    @Test
    void cscanReachesTheEndEvenWhenAllRequestsAreOnTheSweepSide() {
        DiskResult cscan = new CscanDiskScheduler().schedule(
                50, List.of(70, 90, 120, 150), 200, DiskDirection.UP);
        assertEquals(List.of(50, 70, 90, 120, 150, 199), cscan.path());
        assertEquals(149, cscan.totalHeadMovement());
    }

    @Test
    void rejectsRequestsOutsideTheDisk() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> DiskInputs.validate(53, List.of(150, 37), 100));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> DiskInputs.validate(-1, List.of(10, 20), 100));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> DiskInputs.validate(10, List.of(10, 20), 0));
        // A valid request set does not throw.
        DiskInputs.validate(53, REQUESTS, SIZE);
    }

    @Test
    void everyRequestGetsServiced() {
        DiskResult result = run(new SstfDiskScheduler());
        for (int request : REQUESTS) {
            assertEquals(true, result.path().contains(request),
                    "request " + request + " should be visited");
        }
    }
}
