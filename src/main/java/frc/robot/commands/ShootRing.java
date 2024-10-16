// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import frc.robot.commands.speaker.AutoAlign;
import frc.robot.commands.speaker.PivotShooterSetUp;
import frc.robot.commands.speaker.StopShooter;
import frc.robot.subsystems.Photonvision;
import frc.robot.subsystems.Pivot;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.utils.GlobalsValues.PivotGlobalValues;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ShootRing extends SequentialCommandGroup {
  /** Creates a new ShootRing. */
  private Shooter shooter;

  private Pivot pivot;
  private SwerveSubsystem swerveSubsystem;
  private Photonvision photonvision;

  public ShootRing(
      Shooter shooter, Pivot pivot, SwerveSubsystem swerveSubsystem, Photonvision photonvision) {
    this.shooter = shooter;
    this.pivot = pivot;
    this.swerveSubsystem = swerveSubsystem;
    this.photonvision = photonvision;
    addRequirements(shooter, pivot, swerveSubsystem, photonvision);

    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
      new ParallelCommandGroup(
        new PivotShooterSetUp(pivot, shooter, photonvision).withTimeout(0.6),
        new AutoAlign(swerveSubsystem, photonvision).withTimeout(0.6)),
      new PushRing(shooter, photonvision, true).withTimeout(0.5),
      new StopShooter(shooter).withTimeout(0.01),
      new SetPivot(pivot, PivotGlobalValues.PIVOT_NEUTRAL_ANGLE).withTimeout(0.4));
  }
}
