package com.example.demo.ui;

import com.example.demo.model.*;

import com.example.demo.repository.*;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoggedUI extends VerticalLayout {
    private UserRepository userRepository;
    private ProjectRepository projectRepository;
    private ProjectParticipationRepository projectParticipationRepository;
    private TaskRepository taskRepository;
    private SprintRepository sprintRepository;

    private User user;
    private HorizontalLayout horizontalLayout;
    private VerticalLayout verticalLayout;
    private Button createProjectButton;
    private Button sendInvitationToProject;
    private Button manageSprintsButton;
    private Button manageTasksButton;
    private Button informationProjectButton;

    private VerticalLayout createProjectLayout;
    private VerticalLayout sendInvitationLayout;
    private VerticalLayout manageSprintLayout;
    private VerticalLayout manageTaskLayout;
    private VerticalLayout informationProjectLayout;

    private List<Project> allProjectList;
    private ListDataProvider<Project> projectsAdministratorProvider;
    private ListDataProvider<Project> allProjectsProvider;
    private ListDataProvider<Project> projectsUserParticipationProvider;

    public LoggedUI(User user, UserRepository userRepository, ProjectRepository projectRepository, ProjectParticipationRepository
            projectParticipationRepository, TaskRepository taskRepository, SprintRepository sprintRepository) {
        this.user = user;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectParticipationRepository = projectParticipationRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        horizontalLayout = new HorizontalLayout();
        verticalLayout = new VerticalLayout();

        allProjectList = projectRepository.findAll();

        allProjectsProvider = DataProvider.ofCollection(allProjectList);
        allProjectsProvider.setSortComparator((o1, o2) -> o1.getName().compareTo(o2.getName()));

        projectsAdministratorProvider = DataProvider.ofCollection(allProjectList);
        projectsAdministratorProvider.setFilter(project -> project.getLeaderUser().equals(user));
        projectsAdministratorProvider.setSortComparator((o1, o2) -> o1.getName().compareTo(o2.getName()));

        projectsUserParticipationProvider = DataProvider.ofCollection(allProjectList);
        projectsUserParticipationProvider.setFilter(project -> projectParticipationRepository
                .findAllByUserAndProject(user, project)
                .size() > 0
        );
        projectsUserParticipationProvider.setSortComparator((o1, o2) -> o1.getName().compareTo(o2.getName()));

        initCreateProjectLayout();
        initSendInvitationLayout();
        initManageSprintsLayout();
        initManageTaskLayout();
        initInformationProjectLayout();

        createProjectButton = new Button("Create project");
        createProjectButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponent(createProjectLayout);
        });

        sendInvitationToProject = new Button("Send invitation");
        sendInvitationToProject.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponent(sendInvitationLayout);
        });

        manageSprintsButton = new Button("Manage sprints");
        manageSprintsButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponent(manageSprintLayout);
        });

        manageTasksButton = new Button("Manage taks");
        manageTasksButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponent(manageTaskLayout);
        });

        informationProjectButton = new Button("Project informations");
        informationProjectButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponent(informationProjectLayout);
        });

        createProjectButton.click();
        horizontalLayout.addComponents(createProjectButton, sendInvitationToProject, manageSprintsButton, manageTasksButton, informationProjectButton);
        addComponents(horizontalLayout, verticalLayout);
    }

    private void initCreateProjectLayout() {
        createProjectLayout = new VerticalLayout();
        TextField nameProject = new TextField("Name");
        TextField descriptionProject = new TextField("Description");
        Button createButton = new Button("Create");
        createButton.addClickListener(event1 -> {
            if (projectRepository.findAllByName(nameProject.getValue()).isEmpty()) {
                Project project = projectRepository.save(
                        new Project(0L, nameProject.getValue(), descriptionProject.getValue(), user));
                projectParticipationRepository.save(
                        new ProjectParticipation(0L, project, user));
                allProjectList.add(project);
                allProjectsProvider.refreshItem(project);
                projectsAdministratorProvider.refreshItem(project);
                projectsUserParticipationProvider.refreshItem(project);
                Notification.show("Project has been created", "",
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show("Project with this name already exists!", "",
                        Notification.Type.ERROR_MESSAGE);
            }
        });
        createProjectLayout.addComponents(nameProject, descriptionProject, createButton);
    }

    private void initSendInvitationLayout() {
        sendInvitationLayout = new VerticalLayout();
        ComboBox<Project> projectComboBox = new ComboBox<>("Choose project");
        projectComboBox.setEmptySelectionAllowed(false);
        projectComboBox.setDataProvider(projectsAdministratorProvider);
        projectComboBox.setItemCaptionGenerator(Project::getName);
        projectComboBox.setWidth("250");

        ComboBox<User> userComboBox = new ComboBox<>("Choose user");
        userComboBox.setEmptySelectionAllowed(false);
        userComboBox.setItemCaptionGenerator(User::getEmail);
        userComboBox.setWidth("250");
        projectComboBox.addValueChangeListener(event1 ->
                userComboBox.setItems(userRepository.findAll().stream().filter(user1 ->
                        projectParticipationRepository.findAllByUserAndProject(user1, event1.getValue()).size() == 0))
        );

        Button sendInvitationButton = new Button("Send");
        sendInvitationButton.addClickListener(event1 -> {
            if (projectComboBox.getValue() != null && userComboBox.getValue() != null) {
                projectParticipationRepository.save(
                        new ProjectParticipation(0L, projectComboBox.getValue(), userComboBox.getValue()));
                Notification.show("Invitation has been sent", "",
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show("Field is empty", "",
                        Notification.Type.ERROR_MESSAGE);
            }
        });
        sendInvitationLayout.addComponents(projectComboBox, userComboBox, sendInvitationButton);
    }

    private void initManageSprintsLayout() {
        manageSprintLayout = new VerticalLayout();
        ComboBox<Project> projectComboBox = new ComboBox<>("Choose project");
        projectComboBox.setEmptySelectionAllowed(false);
        projectComboBox.setDataProvider(projectsAdministratorProvider);
        projectComboBox.setItemCaptionGenerator(Project::getName);

        Grid<Sprint> sprintGrid = new Grid<>();
        sprintGrid.addColumn(Sprint::getId).setCaption("No.");
        sprintGrid.addColumn(Sprint::getFromLocalDate).setCaption("From");
        sprintGrid.addColumn(Sprint::getToLocalDate).setCaption("To");
        sprintGrid.addColumn(Sprint::getStoryPointsPlanned).setCaption("Planned story points");
        sprintGrid.setWidth("650");
        sprintGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        List<Sprint> sprintList = new ArrayList<>();
        ListDataProvider<Sprint> provider = DataProvider.ofCollection(sprintList);
        sprintGrid.setDataProvider(provider);

        projectComboBox.addValueChangeListener(event1 -> {
                    sprintList.clear();
                    sprintList.addAll(sprintRepository.findAllByProject(event1.getValue()));
                    provider.refreshAll();
                }
        );

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        DateField fromDateField = new DateField("Select start of sprint");
        fromDateField.setValue(LocalDate.now());
        fromDateField.setTextFieldEnabled(false);
        DateField toDateField = new DateField("Select end of sprint");
        toDateField.setValue(LocalDate.now().plusDays(7L));
        toDateField.setTextFieldEnabled(false);
        TextField storyPointsPlannedTextField = new TextField("Planned story points");

        Button addButton = new Button("Add sprint");
        addButton.addClickListener(event -> {
            try {
                if (toDateField.getValue().isAfter(fromDateField.getValue())) {
                    if (sprintRepository.findAllByProject(projectComboBox.getValue()).stream().allMatch(sprint ->
                            fromDateField.getValue().isAfter(sprint.getToLocalDate()) || toDateField.getValue().isBefore(sprint.getFromLocalDate()))) {
                        Sprint sprint = sprintRepository.save(new Sprint(0L, fromDateField.getValue(),
                                toDateField.getValue(), Integer.valueOf(storyPointsPlannedTextField.getValue()), projectComboBox.getValue()));
                        sprintList.add(sprint);
                        provider.refreshAll();
                    } else {
                        Notification.show("Sprint will overlap with other!", "",
                                Notification.Type.ERROR_MESSAGE);
                    }
                } else {
                    Notification.show("Wrong dates!", "",
                            Notification.Type.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                Notification.show("Wrong number of planned story points", "",
                        Notification.Type.ERROR_MESSAGE);
            }
        });

        Button deleteButton = new Button("Delete");
        deleteButton.addClickListener(event -> {
            if (!sprintGrid.getSelectedItems().isEmpty()) {
                Sprint sprint = sprintGrid.getSelectedItems().iterator().next();
                sprintList.remove(sprint);
                provider.refreshAll();
                taskRepository.deleteAllBySprint(sprint);
                sprintRepository.delete(sprint);
                Notification.show("Sprint has been deleted", "",
                        Notification.Type.HUMANIZED_MESSAGE);
            }
        });
        horizontalLayout.addComponents(deleteButton, addButton);
        manageSprintLayout.addComponents(projectComboBox, sprintGrid, horizontalLayout, fromDateField, toDateField, storyPointsPlannedTextField);
    }

    private void initManageTaskLayout() {
        manageTaskLayout = new VerticalLayout();
        ComboBox<Project> projectComboBox = new ComboBox<>("Select project");
        projectComboBox.setEmptySelectionAllowed(true);
        projectComboBox.setDataProvider(projectsUserParticipationProvider);
        projectComboBox.setItemCaptionGenerator(Project::getName);

        ComboBox<Sprint> sprintComboBox = new ComboBox<>("Select sprint");
        sprintComboBox.setEmptySelectionAllowed(true);
        sprintComboBox.setItemCaptionGenerator(item -> item.getFromLocalDate().toString() +
                " - " + item.getToLocalDate().toString());
        sprintComboBox.setWidth("250");
//        if (!projects.isEmpty()) {
//            projectComboBox.setValue(projects.get(0));
//            sprintComboBox.setValue(sprintRepository.findAllByProject(projects.get(0))
//                    .stream()
//                    .filter(sprint -> !sprint.getFromLocalDate().isBefore(LocalDate.now()))
//                    .min(Comparator.comparing(Sprint::getFromLocalDate)).orElse(new Sprint()));
//        }
        TextField findByNameTextField = new TextField("Find task by name");
        Button chooseToAddTaskButton = new Button("Add task");
        VerticalLayout verticalLayout = new VerticalLayout();
        TextField nameTextField = new TextField("Name");
        TextField descriptionTextField = new TextField("Description");
        TextField wageTextField = new TextField("Wage");
        TextField storyPointsTextField = new TextField("Story points");
        Button saveButton = new Button("Save");

        Button chooseToEditProgressButton = new Button("Edit progress");
        ComboBox<Progress> progressComboBox = new ComboBox<>("Select progress");
        progressComboBox.setEmptySelectionAllowed(false);
        progressComboBox.setItems(Progress.values());
        Button saveProgress = new Button("Save");

        chooseToAddTaskButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponents(nameTextField, descriptionTextField, wageTextField,
                    storyPointsTextField, saveButton);
        });

        chooseToEditProgressButton.addClickListener(event -> {
            verticalLayout.removeAllComponents();
            verticalLayout.addComponents(progressComboBox, saveProgress);
        });

        Grid<Task> taskGrid = new Grid<>();
        taskGrid.addColumn(Task::getName).setCaption("Name");
        taskGrid.addColumn(Task::getDescription).setCaption("Description");
        taskGrid.addColumn(task -> task.getUser().getName()).setCaption("User");
        taskGrid.addColumn(Task::getProgress).setCaption("Progress");
        taskGrid.setWidth("750");
        taskGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        List<Task> taskList = new ArrayList<>();
        ListDataProvider<Task> provider = DataProvider.ofCollection(taskList);
        provider.setSortComparator((o1, o2) -> o1.getProgress().getValue() - o2.getProgress().getValue());
        taskGrid.setDataProvider(provider);

        projectComboBox.addValueChangeListener(event ->
                sprintComboBox.setItems(sprintRepository.findAllByProject(event.getValue()))
        );
        sprintComboBox.addValueChangeListener(event -> {
            taskList.clear();
            taskList.addAll(taskRepository.findAllBySprint(event.getValue()));
            provider.refreshAll();
        });
        findByNameTextField.addValueChangeListener(event -> {
            taskList.clear();
            taskList.addAll(
                    taskRepository.findAllByNameStartingWithIgnoreCase(event.getValue()));
            provider.refreshAll();
        });

        saveButton.addClickListener(event -> {
            if (!sprintComboBox.isEmpty()) {
                if (!nameTextField.isEmpty() && !descriptionTextField.isEmpty() &&
                        !wageTextField.isEmpty() && !storyPointsTextField.isEmpty()) {
                    try {
                        Task task = taskRepository.save(
                                new Task(0L, nameTextField.getValue(), descriptionTextField.getValue(),
                                        sprintComboBox.getValue(), Integer.valueOf(wageTextField.getValue()),
                                        Integer.valueOf(storyPointsTextField.getValue()), Progress.TODO, user));
                        Sprint sprint = task.getSprint();
                        sprint.setStoryPointsPlanned(sprint.getStoryPointsPlanned() + Integer.valueOf(storyPointsTextField.getValue()));
                        sprintRepository.save(sprint);
                        taskList.add(task);
                        provider.refreshAll();
                    } catch (NumberFormatException e) {
                        Notification.show("Wrong number!", "",
                                Notification.Type.ERROR_MESSAGE);
                    }
                } else {
                    Notification.show("Empty field!", "",
                            Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show("Select sprint from ComboBox!", "",
                        Notification.Type.ERROR_MESSAGE);
            }
        });

        saveProgress.addClickListener(event -> {
            if (taskGrid.getSelectedItems().size() == 1) {
                if (!progressComboBox.isEmpty()) {
                    Task task = taskGrid.getSelectedItems().iterator().next();
                    int index = taskList.indexOf(task);
                    task.setProgress(progressComboBox.getValue());
                    task = taskRepository.save(task);
                    taskList.set(index, task);
                    provider.refreshAll();
                } else {
                    Notification.show("Select progress from ComboBox!", "",
                            Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show("Select one task from grid!", "",
                        Notification.Type.ERROR_MESSAGE);
            }
        });

        HorizontalLayout findTasksHorizontalLayout = new HorizontalLayout();
        findTasksHorizontalLayout.addComponents(projectComboBox, sprintComboBox, findByNameTextField);
        HorizontalLayout addTasksHorizontalLayout = new HorizontalLayout();
        addTasksHorizontalLayout.addComponents(chooseToAddTaskButton, chooseToEditProgressButton);
        manageTaskLayout.addComponents(findTasksHorizontalLayout, taskGrid, addTasksHorizontalLayout, verticalLayout);
    }

    private void initInformationProjectLayout() {
        informationProjectLayout = new VerticalLayout();
        ComboBox<Project> projectComboBox = new ComboBox<>("Projects");
        projectComboBox.setEmptySelectionAllowed(true);
        projectComboBox.setDataProvider(allProjectsProvider);
        projectComboBox.setItemCaptionGenerator(Project::getName);

        ComboBox<Sprint> sprintComboBox = new ComboBox<>("Sprint");
        sprintComboBox.setEmptySelectionAllowed(true);
        sprintComboBox.setItemCaptionGenerator(item -> item.getFromLocalDate().toString() +
                " - " + item.getToLocalDate().toString());
        sprintComboBox.setWidth("250");

        ProgressBar progressBar = new ProgressBar(0.0F);
        progressBar.setCaption("Story points");
        progressBar.setWidth("150px");

        ComboBox<User> userComboBox = new ComboBox<>("Users");
        userComboBox.setEmptySelectionAllowed(true);
        userComboBox.setItemCaptionGenerator(User::getName);

        Grid<Task> taskGrid = new Grid<>();
        taskGrid.addColumn(Task::getName).setCaption("Name");
        taskGrid.addColumn(Task::getDescription).setCaption("Description").setWidth(200.0);
        taskGrid.addColumn(Task::getWage).setCaption("Wage");
        taskGrid.addColumn(Task::getStoryPoints).setCaption("Story points");
        taskGrid.addColumn(Task::getProgress).setCaption("Progress");
        taskGrid.setWidth("750");
        taskGrid.setHeight("300");
        taskGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        projectComboBox.addValueChangeListener(event -> {
            sprintComboBox.setItems(sprintRepository.findAllByProject(event.getValue()));
            userComboBox.setItems(
                    projectParticipationRepository.findAllByProject(event.getValue())
                            .stream()
                            .map(ProjectParticipation::getUser)
                            .distinct()
            );
        });

        sprintComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                float doneStoryPoints = taskRepository.findAllBySprintAndAndProgress(sprintComboBox.getValue(), Progress.DONE).stream().mapToInt(Task::getStoryPoints).sum();
                float plannedStoryPoints = sprintComboBox.getValue().getStoryPointsPlanned();
                progressBar.setValue(doneStoryPoints / plannedStoryPoints);
            } else {
                progressBar.setValue(0.0F);
            }
        });

        userComboBox.addValueChangeListener(event -> taskGrid.setItems(
                taskRepository.findAllByUser(event.getValue())
                        .stream()
                        .filter(task -> task.getSprint()
                                .getProject().equals(projectComboBox.getValue()))
        ));

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponents(projectComboBox, sprintComboBox, progressBar);
        informationProjectLayout.addComponents(horizontalLayout, userComboBox, taskGrid);
    }

}
