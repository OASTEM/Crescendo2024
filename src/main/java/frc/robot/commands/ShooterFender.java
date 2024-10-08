// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Photonvision;
import frc.robot.subsystems.Pivot;
import frc.robot.subsystems.Shooter;
import frc.robot.utils.GlobalsValues.PivotGlobalValues;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ShooterFender extends SequentialCommandGroup {
  /** Creates a new ShooterFender. */
  private Shooter shooter;

  private Pivot pivot;
  private Photonvision photonvision;

  public ShooterFender(Shooter shooter, Pivot pivot, Photonvision photonvision) {
    this.shooter = shooter;
    this.pivot = pivot;
    this.photonvision = photonvision;
    addRequirements(shooter, pivot);

    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
        new ParallelCommandGroup(
            new SetPivot(pivot, PivotGlobalValues.PIVOT_FENDER_ANGLE).withTimeout(1),
            new ShooterRampUp(shooter, photonvision).withTimeout(1)),
        new PushRing(shooter, photonvision, true).withTimeout(0.3),
        new StopShooter(shooter),
        new SetPivot(pivot, PivotGlobalValues.PIVOT_NEUTRAL_ANGLE).withTimeout(0.3));
  }
}
