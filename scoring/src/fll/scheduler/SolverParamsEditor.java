/*
 * Copyright (c) 2016 High Tech Kids.  All rights reserved
 * HighTechKids is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.scheduler;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import fll.util.CSVCellReader;
import fll.util.FormatterUtils;
import net.mtu.eggplant.util.BasicFileFilter;

/**
 * Editor for {@link SolverParams}.
 */
public class SolverParamsEditor extends JPanel implements MouseListener, ActionListener {

  private final ScheduleTimeField startTimeEditor;

  private final JCheckBox alternateTables;

  private final JCheckBox randomize_teams;

  private long random_seed = -1;

  private final JFormattedTextField performanceDuration;

  private final SubjectiveStationListEditor subjectiveStations;

  private final JFormattedTextField changeDuration;

  private final JFormattedTextField performanceChangeDuration;

  private final JFormattedTextField numPerformanceRounds;

  private final JCheckBox subjectiveFirst;

  private final JFormattedTextField perfAttemptOffsetMinutes;

  private final JFormattedTextField subjectiveAttemptOffsetMinutes;

  private final JFormattedTextField numTables;

  private final ScheduleDurationField maxTime;

  private final JudgingGroupListEditor judgingGroups;

  private final JButton chooseTeamFile;
  private int[] teamNumbers;

  private final ScheduledBreakListEditor subjectiveBreaks;

  private final ScheduledBreakListEditor performanceBreaks;

  public SolverParamsEditor() {
    super(new GridBagLayout());

    GridBagConstraints gbc;

    startTimeEditor = new ScheduleTimeField();
    startTimeEditor.setInputVerifier(new ScheduleTimeField.TimeVerifier());
    addRow(new JLabel("Start Time:"), startTimeEditor);

    alternateTables = new JCheckBox("Alternate performance table start time");
    // alternateTables.add
    alternateTables.setToolTipText("If this is selected every odd table will be offset by 1/2 the time of the performance duration");
    addRow(alternateTables);

    randomize_teams = new JCheckBox("Randomize the teams when creating the schedule");
    randomize_teams.setToolTipText("This will shuffle the list of teams before assigning them to a performance time.");
    addRow(randomize_teams);

    performanceDuration = FormatterUtils.createIntegerField(1, 1000);
    performanceDuration.setToolTipText("The number of minutes each performance table will be 'occupied' for");
    addRow(new JLabel("Performance duration:"), performanceDuration);

    subjectiveStations = new SubjectiveStationListEditor();
    addRow(subjectiveStations);

    changeDuration = FormatterUtils.createIntegerField(0, 1000);
    changeDuration.setToolTipText("The number of minutes that a team has between any 2 activities");
    addRow(new JLabel("Teams change time duration:"), changeDuration);

    performanceChangeDuration = FormatterUtils.createIntegerField(0, 1000);
    performanceChangeDuration.setToolTipText("The number of minutes that a team has between any 2 performance runs");
    addRow(new JLabel("Teams performance change time duration:"), performanceChangeDuration);

    chooseTeamFile = new JButton("Select a file");
    chooseTeamFile.setToolTipText("This loads a CSV file with all the team numbers.");
    chooseTeamFile.addActionListener(this);
    addRow(new JLabel("Load a team file to use"), chooseTeamFile);

    judgingGroups = new JudgingGroupListEditor();
    addRow(judgingGroups);

    numPerformanceRounds = FormatterUtils.createIntegerField(0, 10);
    addRow(new JLabel("Number of performance rounds:"), numPerformanceRounds);

    subjectiveFirst = new JCheckBox("Schedule all subjective rounds before performance");
    addRow(subjectiveFirst);

    perfAttemptOffsetMinutes = FormatterUtils.createIntegerField(1, 1000);
    perfAttemptOffsetMinutes.setToolTipText("How many minutes later to try to find a new performance time slot when no team can be scheduled at a time.");
    addRow(new JLabel("Time increment between each performance session"), perfAttemptOffsetMinutes);

    subjectiveAttemptOffsetMinutes = FormatterUtils.createIntegerField(1, 1000);
    subjectiveAttemptOffsetMinutes.setToolTipText("How many minutes later to try to find a new subjective time slot when no team can be scheduled at a time.");
    addRow(new JLabel("Time increment between each subjective session"), subjectiveAttemptOffsetMinutes);

    numTables = FormatterUtils.createIntegerField(1, 1000);
    addRow(new JLabel("Number of performance tables"), numTables);

    maxTime = new ScheduleDurationField();
    maxTime.setToolTipText("Maximum duration of the tournament hours:minutes");
    addRow(new JLabel("Maximum length of the tournament"), maxTime);

    subjectiveBreaks = new ScheduledBreakListEditor("Subjective Breaks");
    addRow(subjectiveBreaks);

    performanceBreaks = new ScheduledBreakListEditor("Performance Breaks");
    addRow(performanceBreaks);

    // end of form spacer
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 1.0;
    add(new JPanel(), gbc);
  }

  /**
   * Add a row of components and then add a spacer to the end of the row.
   * 
   * @param components the components to add
   */
  private void addRow(final JComponent... components) {
    GridBagConstraints gbc;

    // for (final JComponent comp : components) {
    for (int i = 0; i < components.length - 1; ++i) {
      final JComponent comp = components[i];
      gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weighty = 0;
      add(comp, gbc);
    }

    // end of line spacer
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    // add(new JPanel(), gbc);
    add(components[components.length - 1], gbc);
  }

  private SolverParams params;

  public void setParams(final SolverParams params) {
    this.params = params;

    startTimeEditor.setTime(params.getStartTime());
    alternateTables.setSelected(this.params.getAlternateTables());
    randomize_teams.setSelected(params.shouldRandomizeTeams());
    random_seed = params.getRandomSeed();
    performanceDuration.setValue(params.getPerformanceMinutes());
    changeDuration.setValue(params.getChangetimeMinutes());
    performanceChangeDuration.setValue(params.getPerformanceChangetimeMinutes());

    subjectiveStations.setStations(params.getSubjectiveStations());

    judgingGroups.setJudgingGroups(params.getJudgingGroups());

    numPerformanceRounds.setValue(params.getNumPerformanceRounds());
    subjectiveFirst.setSelected(params.getSubjectiveFirst());
    perfAttemptOffsetMinutes.setValue(params.getPerformanceAttemptOffsetMinutes());
    subjectiveAttemptOffsetMinutes.setValue(params.getSubjectiveAttemptOffsetMinutes());
    numTables.setValue(params.getNumTables());

    maxTime.setDuration(params.getMaxDuration());

    subjectiveBreaks.setBreaks(params.getSubjectiveBreaks());
    performanceBreaks.setBreaks(params.getPerformanceBreaks());

  }

  /**
   * The values from the editors are pushed into the parameters object and that
   * object is returned.
   * The caller should call {@link SchedParams#isValid()} called on it and
   * display the errors to the user.
   * 
   * @return a non-null parameters object
   */
  public SolverParams getParams() {
    params = new SolverParams();

    params.setStartTime(startTimeEditor.getTime());
    params.setAlternateTables(alternateTables.isSelected());
    params.setRandomizeTeams(randomize_teams.isSelected());
    params.setRandomSeed(random_seed);
    params.setPerformanceMinutes((Integer) performanceDuration.getValue());
    params.setChangetimeMinutes((Integer) performanceChangeDuration.getValue());
    params.setPerformanceChangetimeMinutes((Integer) performanceChangeDuration.getValue());

    params.setSubjectiveStations(subjectiveStations.getStations());

    final Map<String, Integer> judgingGroupMap = judgingGroups.getJudgingGroups();
    params.setJudgingGroups(judgingGroupMap);

    params.setNumPerformanceRounds((Integer) numPerformanceRounds.getValue());
    params.setSubjectiveFirst(subjectiveFirst.isSelected());
    params.setPerformanceAttemptOffsetMinutes((Integer) perfAttemptOffsetMinutes.getValue());
    params.setSubjectiveAttemptOffsetMinutes((Integer) subjectiveAttemptOffsetMinutes.getValue());
    params.setNumTables((Integer) numTables.getValue());

    params.setMaxDuration(maxTime.getDuration());

    params.setSubjectiveBreaks(subjectiveBreaks.getBreaks());
    params.setPerformanceBreaks(performanceBreaks.getBreaks());

    return this.params;
  }

  public void setRandomSeed(long seed) {
    this.random_seed = seed;
  }

  public long getSeed() {
    return this.random_seed;
  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    // TODO Auto-generated method stub

  }
  
  public int[] getTeamNumbers() {
    return teamNumbers;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() != chooseTeamFile)
      return;

    JFileChooser teamFileSelector = new JFileChooser();
    FileFilter csvFilter = new BasicFileFilter("Team files", "csv");
    teamFileSelector.setFileFilter(csvFilter);

    final int returnValue = teamFileSelector.showOpenDialog(this);
    if (returnValue == JFileChooser.APPROVE_OPTION) {
      try {
        File file = teamFileSelector.getSelectedFile();
        if (file.exists()) {
          System.out.println("Selected file " + file.getAbsolutePath());
          CSVCellReader csvReader = new CSVCellReader(file);
          String[] reader = csvReader.readNext();
          if (reader == null) {
            return;
          }
          int col = 0;
          int teamNumberCol = -1;
          for (String line : reader) {
            if (line.equalsIgnoreCase("team") || line.equalsIgnoreCase("team number")
                || line.equalsIgnoreCase("team #")) {
              teamNumberCol = col;
              break;
            }
            System.out.println(line);
            col++;
          }
          int teams = 0;
          String[] line;
          ArrayList<Integer> allTeams = new ArrayList<Integer>();
          for(;(line = csvReader.readNext()) != null; teams++) {
            if(line.length > teamNumberCol) {
              int teamNumber = Integer.valueOf(line[teamNumberCol]);
              if(teamNumber > 0) {
                allTeams.add(teamNumber);
              }
            }else {
              System.err.println("Could not find team");
            }
          }
          this.teamNumbers = new int[allTeams.size()];
          int index = 0;
          for(int teamNumber : allTeams) {
            teamNumbers[index] = teamNumber;
            index++;
          }
          params.setTeamNumbers(teamNumbers);
//          System.out.println("Total number of teams " + allTeams.size());
//          for(int teamNumber : allTeams) {
//            System.out.println("Team : " + teamNumber);
//          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

}
