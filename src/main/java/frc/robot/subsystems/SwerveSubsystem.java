package frc.robot.subsystems;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.pathplanner.lib.auto.AutoBuilder;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;

// import edu.wpi.first.math.controller.PIDController;
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.utils.GlobalsValues;
import frc.robot.utils.GlobalsValues.MotorGlobalValues;
import frc.robot.utils.GlobalsValues.SwerveGlobalValues;

/**
 * The {@link SwerveSubsystem} class includes all the motors to drive the robot.
 */
public class SwerveSubsystem extends SubsystemBase {
  // Variables for the swerve drive train
  private SwerveModule[] modules;
  private Rotation2d gyroAngle;
  private Pigeon2 pidggy;
  private final SwerveDriveKinematics sKinematics;

  public SwerveDriveOdometry swerveOdometry;
  public SwerveDrivePoseEstimator swerveEstimator;
  public Pose2d swerveOdomeryPose2d;

  public Field2d field;

  private double rot;
  private double turnSpeed = 0;

  /** Creates a new DriveTrain. */
  public SwerveSubsystem() {
    sKinematics = GlobalsValues.SwerveGlobalValues.kinematics;
    gyroAngle = Rotation2d.fromDegrees(0);
    pidggy = new Pigeon2(16);
    pidggy.reset();

    modules = new SwerveModule[] {
        new SwerveModule(
            MotorGlobalValues.FRONT_LEFT_DRIVE_ID,
            MotorGlobalValues.FRONT_LEFT_STEER_ID,
            MotorGlobalValues.FRONT_LEFT_CAN_CODER_ID,
            SwerveGlobalValues.CANCoderValue9),
        new SwerveModule(
            MotorGlobalValues.FRONT_RIGHT_DRIVE_ID,
            MotorGlobalValues.FRONT_RIGHT_STEER_ID,
            MotorGlobalValues.FRONT_RIGHT_CAN_CODER_ID,
            SwerveGlobalValues.CANCoderValue10),
        new SwerveModule(
            MotorGlobalValues.BACK_LEFT_DRIVE_ID,
            MotorGlobalValues.BACK_LEFT_STEER_ID,
            MotorGlobalValues.BACK_LEFT_CAN_CODER_ID,
            SwerveGlobalValues.CANCoderValue11),
        new SwerveModule(
            MotorGlobalValues.BACK_RIGHT_DRIVE_ID,
            MotorGlobalValues.BACK_RIGHT_STEER_ID,
            MotorGlobalValues.BACK_RIGHT_CAN_CODER_ID,
            SwerveGlobalValues.CANCoderValue12)
    };

    swerveOdometry = new SwerveDriveOdometry(sKinematics, gyroAngle, getModulePositions());
    swerveOdomeryPose2d = new Pose2d();

    swerveEstimator = new SwerveDrivePoseEstimator(SwerveGlobalValues.kinematics, pidggy.getRotation2d(), getModulePositions(), new Pose2d(),
    VecBuilder.fill(0.05, 0.05, Math.toRadians(5)),
    VecBuilder.fill(0.5, 0.5, Math.toRadians(30))); 
    // addRotorPositionsforModules();

    // Makes the rotation smooth (in a circle)
    // Blame Jayden for adding a useless line of code
    SwerveGlobalValues.BasePIDGlobal.pathTranslationPID.enableContinuousInput(-Math.PI, Math.PI);

    field = new Field2d();
    field.setRobotPose(swerveEstimator.getEstimatedPosition());
    
    /**
     * PathPlanner Direction Values
     * Forward +x
     * Backward -x
     * Left -y
     * Right +y
     */ 
    AutoBuilder.configureHolonomic(
        this::getPose, // Robot pose supplier
        this::customPose, // Method to reset odometry (will be called if your auto has a starting pose)
        this::getAutoSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
        this::chassisSpeedsDrive, // Method that will drive the robot given ROBOT RELATIVE ChassisSpeeds
        SwerveGlobalValues.BasePIDGlobal.pathFollower,
        () -> {
          var alliance = DriverStation.getAlliance();
          if (alliance.isPresent()) {
            return !(alliance.get() == DriverStation.Alliance.Red);
          }
          return false;
        },
        this // Reference to this subsystem to set requirements
    );
  }

  /**
   * Drives the robot using the joystick input
   * 
   * @param forwardSpeed double Speed value in meters per second
   * @param leftSpeed double Speed value in meters per second
   * @param joyStickInput double joystick input value
   * @param isFieldOriented boolean value to determine if the robot is field oriented
   * @return void
   */
  public void drive(double forwardSpeed, double leftSpeed, double joyStickInput, boolean isFieldOriented) {
    ChassisSpeeds speeds;

    turnSpeed = joyStickInput * MotorGlobalValues.TURN_CONSTANT;

    System.out.println("Forward Speed: " + forwardSpeed);
    System.out.println("Left Speed: " + leftSpeed);
    System.out.println("Turn Speed: " + joyStickInput);
    // Runs robot/field-oriented based on the boolean value of isFieldOriented
    if (isFieldOriented) {
      speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
          forwardSpeed,
          leftSpeed,
          turnSpeed,
          getRotationPidggy());
    } else {
      speeds = new ChassisSpeeds(
          forwardSpeed,
          leftSpeed,
          joyStickInput);
    }

    SwerveModuleState[] states = SwerveGlobalValues.kinematics.toSwerveModuleStates(speeds);
    SwerveDriveKinematics.desaturateWheelSpeeds(
        states, MotorGlobalValues.MAX_SPEED);

    for (int i = 0; i < modules.length; i++) {
      modules[i].setState(states[i]);

    }
  }

  /**
   * Gets the position of the modules in the swerve drive train
   * @param void
   * @return SwerveModulePosition[] array of the position of the modules
   */
  public SwerveModulePosition[] getModulePositions() {
    SwerveModulePosition[] positions = new SwerveModulePosition[modules.length];
    for (int i = 0; i < modules.length; i++) {
      positions[i] = modules[i].getPosition();
    }
    return positions;
  }

  /**
   * Gets the rotation of the pigeon
   * @param void
   * @return Rotation2d rotation of the pigeon in degrees
   */
  public Rotation2d getRotationPidggy() {
    rot = -pidggy.getRotation2d().getDegrees();
    return Rotation2d.fromDegrees(rot);
  }

  /**
   * Resets the pigeon to zero
   * @param void
   * @return None
   */
  public void zeroHeading() {
    pidggy.reset();
  }

  /**
   * Resets the drive encoders
   * @param void
   * @return None
   */
  public void resetDriveEncoders() {
    for (int i = 0; i < modules.length; i++) {
      modules[i].resetEncoders();
    }
  }

  /**
   * Gets the yaw of the pigeon in double degrees
   * @param void
   * @return double yaw of the pigeon in degrees
   */
  public double getYaw() {
    return pidggy.getYaw().getValue();
  }

  /**
   * Gets the heading of the pigeon in double degrees
   * @param void
   * @return double heading of the pigeon in degrees
   */
  public double pgetHeading() {
    return (pidggy.getYaw().getValue() % 360);
  }

  // Speed modifiers
  /**
   * Configures the slow mode of the robot
   * @param void
   * @return None
   */
  public void configSlowMode() {
    MotorGlobalValues.SLOW_MODE = !MotorGlobalValues.SLOW_MODE;
  }

  /**
   * Gets the slow mode of the robot in boolean value
   * @param void
   * @return boolean value of the slow mode
   */
  public boolean getSlowMode() {
    return MotorGlobalValues.SLOW_MODE;
  }

  /**
   * Configures the acorn mode of the robot
   * @param void
   * @return None
   */
  public void configAAcornMode() {
    MotorGlobalValues.AACORN_MODE = !MotorGlobalValues.AACORN_MODE;
  }

  /**
   * Gets the acorn mode of the robot in boolean value
   * @param void
   * @return boolean value of the acorn mode
   */
  public boolean getAAcornMode() {
    return MotorGlobalValues.AACORN_MODE;
  }

  // Position methods - used for odometry
  /**
   * Gets the pose of the robot in Pose2d
   * @param void
   * @return Pose2d pose of the robot
   */
  public Pose2d getPose() {
    return swerveOdometry.getPoseMeters();
  }

  /**
   * Resets the heading of the robot to 0
   * @param void
   * @return None
   */
  public void newPose() {
    swerveOdometry.resetPosition(Rotation2d.fromDegrees(pgetHeading()), getModulePositions(),
        new Pose2d(0, 0, Rotation2d.fromDegrees(0)));
  }

  /**
   * Resets the pose of the robot to a custom pose
   * @param poses Pose2d pose of the robot
   * @return None
   */
  public void customPose(Pose2d pose) {
    swerveOdometry.resetPosition(pidggy.getRotation2d(), getModulePositions(), pose);
  }

  /**
   * Updates odometry and gets heading of the pigeon
   * @param void
   * @return None
   */
  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    pidggy.getYaw().refresh();
    swerveEstimator.update(pidggy.getRotation2d(), getModulePositions());
    field.setRobotPose(swerveEstimator.getEstimatedPosition());

    Rotation2d headingGyroAnglething = Rotation2d.fromDegrees(pgetHeading());
    swerveOdomeryPose2d = swerveOdometry.update(headingGyroAnglething, getModulePositions());

    SmartDashboard.putNumber("heading", pgetHeading());
    for (int i = 0; i < modules.length; i++) {
      SmartDashboard.putNumber("Module Angle: " + i, modules[i].getCanCoderValueDegrees());
    }

    for (int i = 0; i < modules.length; i++) {
      SmartDashboard.putNumber("Module Speed: " + i, modules[i].getDriveVelocity());
    }
  }

  /**
   * Adds rotor positions for the modules
   * @param void
   * @return None
   */
  public void addRotorPositionsforModules() {
    for (int i = 0; i < modules.length; i++) {
      modules[i].setRotorPos();
    }
  }

  /**
   * Resets odometry to a custom pose
   * @param pose Pose2d
   * @return None
   */
  public void resetOdometry(Pose2d pose) {
    swerveOdometry.resetPosition(Rotation2d.fromDegrees(getYaw()), getModulePositions(), pose);
  }

  /**
   * Passes in the module states to the modules (speed and rotation)
   * @param states SwerveModuleState[] array of the module states
   * @return None
   */
  public void outputModuleStates(SwerveModuleState[] states) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        states, MotorGlobalValues.MAX_SPEED);

    for (int i = 0; i < modules.length; i++) {
      modules[i].setState(states[i]);
    }
  }

  /**
   * Gets the auto speeds of the robot in meters per second
   * @param void
   * @return ChassisSpeeds auto speeds of the robot
   */
  public ChassisSpeeds getAutoSpeeds() {
    return SwerveGlobalValues.kinematics.toChassisSpeeds(getModuleStates());
  }

  /**
   * Drives the robot using the chassis speeds in meters per second
   * @param chassisSpeeds chassis speeds
   * @return None
   */
  public void chassisSpeedsDrive(ChassisSpeeds chassisSpeeds) {
    SwerveModuleState[] states = SwerveGlobalValues.kinematics.toSwerveModuleStates(chassisSpeeds);
    // SwerveDriveKinematics.desaturateWheelSpeeds(
    //     states, MotorGlobalValues.MAX_SPEED);
    for (int i = 0; i < modules.length; i++) {
      modules[i].setState(states[i]);
    }
  }

  /**
   * Stops the modules
   * @param void
   * @return None
   */
  public void stopModules() {
    for (SwerveModule module : modules) {
      module.stop();
    }
  }

  /**
   * Stops the robot
   * @param void
   * @return None
   */
  public void stop() {
    for (int i = 0; i < modules.length; i++) {
      modules[i].stop();
    }
  }

  private SwerveModuleState[] getModuleStates() {
    SwerveModuleState[] moduleStates = new SwerveModuleState[modules.length];
    for (int i = 0; i < modules.length; i++) {
      moduleStates[i] = modules[i].getState();
    }
    return moduleStates;
  }
}