// package frc.robot.commands;

// import edu.wpi.first.wpilibj2.command.Command;
// import edu.wpi.first.wpilibj2.command.CommandBase;
// import org.photonvision.PhotonCamera;
// import org.photonvision.targeting.PhotonPipelineResult;
// import org.photonvision.targeting.TargetReport;
// import frc.robot.subsystems.DrivetrainSubsystem;

// public class StrafeToAprilTag extends Command {
//     private final PhotonCamera camera;
//     private final DrivetrainSubsystem drivetrain;
//     private final double desiredOffset; // Desired offset in degrees (could be 0 if you want the
// target centered)
//     private final double kP = 0.1; // Proportional gain (adjust based on testing)

//     /**
//      * Constructs a new command to strafe toward an AprilTag while maintaining an offset.
//      *
//      * @param drivetrain   The drivetrain subsystem used for driving.
//      * @param desiredOffset The desired offset angle in degrees from the center.
//      */
//     public StrafeToAprilTag(DrivetrainSubsystem drivetrain, double desiredOffset) {
//         this.drivetrain = drivetrain;
//         this.desiredOffset = desiredOffset;
//         camera = new PhotonCamera("photonvision"); // Ensure "photonvision" matches your camera
// name.
//         addRequirements(drivetrain);
//     }

//     @Override
//     public void execute() {
//         PhotonPipelineResult result = camera.getLatestResult();
//         if (result.hasTargets()) {
//             // Get the best target (you might add more logic to choose between multiple targets)
//             TargetReport bestTarget = result.getBestTarget();
//             double targetYaw = bestTarget.getYaw(); // Yaw in degrees relative to camera center

//             // Calculate error: positive error means target is to the right of desired offset
//             double error = targetYaw - desiredOffset;

//             // Calculate strafe speed using a simple proportional controller
//             double strafeSpeed = kP * error;

//             // Command the drivetrain (assuming drive(forward, strafe, rotation) signature)
//             // Here we only command strafe movement. You can combine it with other motion as
// needed.
//             drivetrain.drive(0.0, strafeSpeed, 0.0);
//         } else {
//             // If no target is found, you might want to stop or initiate a search routine.
//             drivetrain.drive(0.0, 0.0, 0.0);
//         }
//     }

//     @Override
//     public void end(boolean interrupted) {
//         // Stop the drivetrain when the command ends.
//         drivetrain.drive(0.0, 0.0, 0.0);
//     }

//     @Override
//     public boolean isFinished() {
//         // Optionally, end the command when the error is within an acceptable threshold.
//         PhotonPipelineResult result = camera.getLatestResult();
//         if (result.hasTargets()) {
//             double error = result.getBestTarget().getYaw() - desiredOffset;
//             return Math.abs(error) < 1.0; // Consider finished when error is less than 1 degree.
//         }
//         return false;
//     }
// }
