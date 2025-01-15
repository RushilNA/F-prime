package frc.robot.subsystems.flywheel;

import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.Kilograms;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.Pounds;
import static edu.wpi.first.units.Units.RotationsPerSecond;
import static edu.wpi.first.units.Units.Volts;

import com.ctre.phoenix6.sim.TalonFXSimState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.FlywheelSim;

public class FlywheelIOSIM extends FlywheelIOCTRE {

  private final FlywheelSim motorSimModel;
  private final TalonFXSimState leaderSim;
  private final TalonFXSimState followerSim;

  public FlywheelIOSIM() {
    super();

    leaderSim = leader.getSimState();
    followerSim = follower.getSimState();
    DCMotor motor = DCMotor.getKrakenX60Foc(2);

    Distance radius = Inches.of(1.5);
    double moi = Pounds.of(8.0).in(Kilograms) * Math.pow(radius.in(Meters), 2);
    LinearSystem<N1, N1, N1> linearSystem =
        LinearSystemId.createFlywheelSystem(motor, moi, GEAR_RATIO);
    motorSimModel = new FlywheelSim(linearSystem, motor);
  }

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    super.updateInputs(inputs);
    leaderSim.setSupplyVoltage(RobotController.getBatteryVoltage());
    followerSim.setSupplyVoltage(RobotController.getBatteryVoltage());

    // use the motor voltage to calculate new position and velocity
    // using WPILib's DCMotorSim class for physics simulation
    motorSimModel.setInputVoltage(leaderSim.getMotorVoltageMeasure().in(Volts));
    motorSimModel.update(0.020); // assume 20 ms loop time

    // Apply the new rotor position and velocity to the TalonFX;
    // note that this is rotor position/velocity (before gear ratio), but
    // DCMotorSim returns mechanism position/velocity (after gear ratio)
    leaderSim.setRotorVelocity(motorSimModel.getAngularVelocity().times(GEAR_RATIO));
    leaderSim.addRotorPosition(
        motorSimModel.getAngularVelocity().times(GEAR_RATIO).in(RotationsPerSecond) * 0.02);
  }
}
