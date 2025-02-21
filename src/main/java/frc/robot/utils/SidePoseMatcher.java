package frc.robot.utils;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import java.util.Arrays;
import java.util.List;

/**
 * This class stores predefined Pose2d positions for both blue and red alliances. It provides a
 * method to return the closest pose from the appropriate list based on the current robot pose and
 * the alliance reported by the DriverStation.
 */
public class SidePoseMatcher {

  // Hard-coded list of poses for the blue alliance.
  private static final List<Pose2d> bluePoses =
      Arrays.asList(
          new Pose2d(new Translation2d(1.0, 2.0), new Rotation2d(0.0)),
          new Pose2d(new Translation2d(2.5, 3.0), new Rotation2d(0.0)),
          new Pose2d(new Translation2d(4.0, 1.5), new Rotation2d(0.0)));

  // Hard-coded list of poses for the red alliance.
  private static final List<Pose2d> redPoses =
      Arrays.asList(
          new Pose2d(new Translation2d(6.0, 5.0), new Rotation2d(0.0)),
          new Pose2d(new Translation2d(7.5, 4.5), new Rotation2d(0.0)),
          new Pose2d(new Translation2d(9.0, 6.0), new Rotation2d(0.0)));

  /**
   * Iterates over a list of Pose2d and returns the one closest to the currentPose.
   *
   * @param poses The list of Pose2d objects.
   * @param currentPose The current robot pose.
   * @return The closest Pose2d from the list.
   */
  private static Pose2d findClosestInList(List<Pose2d> poses, Pose2d currentPose) {
    Pose2d closestPose = null;
    double minDistance = Double.POSITIVE_INFINITY;
    for (Pose2d pose : poses) {
      double distance = currentPose.getTranslation().getDistance(pose.getTranslation());
      if (distance < minDistance) {
        minDistance = distance;
        closestPose = pose;
      }
    }
    return closestPose;
  }

  /**
   * Returns the closest pose to the currentPose from the list corresponding to the alliance. The
   * alliance is retrieved automatically using DriverStation.getAlliance().
   *
   * @param currentPose The current robot pose.
   * @return The closest Pose2d from the appropriate alliance list, or null if the list is empty.
   */
  public static Pose2d getClosestPose(Pose2d currentPose) {
    // Retrieve the alliance from DriverStation.
    DriverStation.Alliance alliance = DriverStation.getAlliance().get();
    List<Pose2d> selectedPoses;

    // Choose the appropriate list based on the alliance.
    if (alliance == DriverStation.Alliance.Blue) {
      selectedPoses = bluePoses;
    } else if (alliance == DriverStation.Alliance.Red) {
      selectedPoses = redPoses;
    } else {
      // Default to blue if alliance is invalid.
      selectedPoses = bluePoses;
    }

    return findClosestInList(selectedPoses, currentPose);
  }
}
