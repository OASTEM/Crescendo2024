// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

// package frc.robot.subsystems;

// import com.ctre.phoenix6.hardware.TalonFX;
// import com.revrobotics.CANSparkMax;
// import com.revrobotics.CANSparkBase.IdleMode;
// import com.revrobotics.CANSparkLowLevel.MotorType;

// import edu.wpi.first.wpilibj2.command.SubsystemBase;
// import frc.robot.utils.GlobalsValues;

// public class Elevator extends SubsystemBase {
//   private CANSparkMax elevatorMotorSparkMax;
    

//   /** Creates a new Elevator. */
//   public Elevator() {
//     elevatorMotorSparkMax = new CANSparkMax(GlobalsValues.MotorGlobalValues.ELEVATOR_NEO_ID, MotorType.kBrushless);

//     elevatorMotorSparkMax.restoreFactoryDefaults();
//     elevatorMotorSparkMax.setIdleMode(IdleMode.kBrake);
//   }

//   @Override
//   public void periodic() {
//     // This method will be called once per scheduler run
//   }
// }