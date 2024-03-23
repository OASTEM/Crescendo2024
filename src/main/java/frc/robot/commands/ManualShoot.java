// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.Pivot;
import frc.robot.subsystems.Shooter;
import frc.robot.utils.GlobalsValues.PivotGlobalValues;
import frc.robot.utils.GlobalsValues.ShooterGlobalValues;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class ManualShoot extends SequentialCommandGroup {
  /** Creates a new ManualShoot. */
  private Shooter shooter;

  public ManualShoot(Shooter shooter) {
    this.shooter = shooter;
    addRequirements(shooter);

    // Add your commands in the addCommands() call, e.g.
    // addCommands(new FooCommand(), new BarCommand());
    addCommands(
      new ShooterRampUp(shooter, ShooterGlobalValues.SHOOTER_SPEED).withTimeout(1),
      new PushRing(shooter).withTimeout(0.5),
      new StopShooter(shooter)
    );
  }
}
