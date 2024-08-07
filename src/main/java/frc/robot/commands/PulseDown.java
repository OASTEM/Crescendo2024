// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;

public class PulseDown extends Command {
  /** Creates a new PulseDown. */
  private Intake intake;
  private Shooter shooter;
  private Timer timer;
  private boolean isDone;

  public PulseDown(Intake intake, Shooter shooter) {
    this.intake = intake;
    this.shooter = shooter;
    timer = new Timer();
    isDone = false;
    addRequirements(intake, shooter);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {

  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
  if (shooter.getRingSensor()) {
      timer.start();
      while (timer.get() < 0.2) {
        shooter.setKrakenVelocity(-35);
      }

      while (timer.get() < 0.4) {
        shooter.setKrakenVelocity(30);
      }

      shooter.stopKraken();
      shooter.stopShooter();
      intake.stopKraken();
      timer.stop();
      timer.reset();
      isDone = true;
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return isDone;
  }
}
