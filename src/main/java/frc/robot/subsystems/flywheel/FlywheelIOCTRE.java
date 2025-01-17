// Copyright FRC 5712
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package frc.robot.subsystems.flywheel;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Voltage;

/**
 * CTRE (Cross The Road Electronics) implementation of the Flywheel IO interface. This class handles
 * the hardware interface for a dual-motor flywheel system using TalonFX motors.
 */
public class FlywheelIOCTRE implements FlywheelIO {

  /** Gear ratio between the motor and the flywheel output. */
  public static final double GEAR_RATIO = 1.5;

  /** Update frequency for motor status signals in Hz. */
  private static final double STATUS_FRAME_RATE_HZ = 50.0;

  /** Debounce time for motor connection status in seconds. */
  private static final double CONNECTION_DEBOUNCE_TIME = 0.5;

  /** Leader motor controller. */
  public final TalonFX leader;

  /** Follower motor controller. */
  public final TalonFX follower;

  // Status signals for motor feedback
  private final StatusSignal<Angle> leaderPosition;
  private final StatusSignal<AngularVelocity> leaderVelocity;
  private final StatusSignal<Voltage> leaderAppliedVolts;
  private final StatusSignal<Current> leaderStatorCurrent;
  private final StatusSignal<Current> followerStatorCurrent;
  private final StatusSignal<Current> leaderSupplyCurrent;
  private final StatusSignal<Current> followerSupplyCurrent;

  // Debouncers for motor connection status
  private final Debouncer leaderDebounce;
  private final Debouncer followerDebounce;

  /**
   * Constructs a new FlywheelIOCTRE instance. Initializes motor controllers with appropriate
   * configuration and sets up status signals.
   */
  public FlywheelIOCTRE() {
    // Initialize motor controllers
    leader = new TalonFX(14);
    follower = new TalonFX(15);

    // Configure motors
    TalonFXConfiguration config = createMotorConfiguration();
    leader.getConfigurator().apply(config);
    follower.getConfigurator().apply(config);

    // Set up follower motor
    follower.setControl(new Follower(leader.getDeviceID(), true));

    // Initialize status signals
    leaderPosition = leader.getPosition();
    leaderVelocity = leader.getVelocity();
    leaderAppliedVolts = leader.getMotorVoltage();
    leaderStatorCurrent = leader.getStatorCurrent();
    followerStatorCurrent = follower.getStatorCurrent();
    leaderSupplyCurrent = leader.getSupplyCurrent();
    followerSupplyCurrent = follower.getSupplyCurrent();

    // Set up status signal update frequency
    configureStatusSignals();

    // Initialize debouncers
    leaderDebounce = new Debouncer(CONNECTION_DEBOUNCE_TIME);
    followerDebounce = new Debouncer(CONNECTION_DEBOUNCE_TIME);
  }

  /**
   * Creates the motor configuration with appropriate settings.
   *
   * @return The configured TalonFXConfiguration object
   */
  private TalonFXConfiguration createMotorConfiguration() {
    var config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    config.Slot0.kP = 0.4;
    config.Slot0.kI = 0;
    config.Slot0.kD = 0;
    config.Slot0.kS = 0.01;
    config.Slot0.kV = 0.123;
    config.Slot0.kA = 0;
    return config;
  }

  /** Configures the update frequency for all status signals. */
  private void configureStatusSignals() {
    BaseStatusSignal.setUpdateFrequencyForAll(
        STATUS_FRAME_RATE_HZ,
        leaderPosition,
        leaderVelocity,
        leaderAppliedVolts,
        leaderStatorCurrent,
        followerStatorCurrent,
        leaderSupplyCurrent,
        followerSupplyCurrent);
  }

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    // Refresh all status signals
    var leaderStatus =
        BaseStatusSignal.refreshAll(
            leaderPosition,
            leaderVelocity,
            leaderAppliedVolts,
            leaderStatorCurrent,
            leaderSupplyCurrent);
    var followerStatus = BaseStatusSignal.refreshAll(followerStatorCurrent, followerSupplyCurrent);

    // Update connection status with debouncing
    inputs.leaderConnected = leaderDebounce.calculate(leaderStatus.isOK());
    inputs.followerConnected = followerDebounce.calculate(followerStatus.isOK());

    // Update sensor readings
    inputs.position = leaderPosition.getValue();
    inputs.velocity = leaderVelocity.getValue();
    inputs.appliedVoltage = leaderAppliedVolts.getValue();

    // Update current measurements
    inputs.leaderStatorCurrent = leaderStatorCurrent.getValue();
    inputs.followerStatorCurrent = followerStatorCurrent.getValue();
    inputs.leaderSupplyCurrent = leaderSupplyCurrent.getValue();
    inputs.followerSupplyCurrent = followerSupplyCurrent.getValue();

    // Optimize CAN bus utilization
    leader.optimizeBusUtilization(4, 0.1);
    follower.optimizeBusUtilization(4, 0.1);
  }

  @Override
  public void setVelocity(AngularVelocity velocity) {
    leader.setControl(new VelocityVoltage(velocity));
  }

  @Override
  public void stop() {
    leader.stopMotor();
  }
}
