package com.example.demo.ui;

import com.example.demo.model.User;
import com.example.demo.repository.*;
import com.google.common.hash.Hashing;
import com.vaadin.annotations.Theme;
import com.vaadin.server.*;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringUI
@Theme("mytheme")
public class LoginSignUpUI extends UI {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectParticipationRepository projectParticipationRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    SprintRepository sprintRepository;

    private VerticalLayout root;
    private VerticalLayout verticalLayout;
    private Button loginButton;
    private Button signUpButton;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Page.getCurrent().setTitle("Login");
        root = new VerticalLayout();
        root.setSpacing(true);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        loginButton = new Button("Choose to login");
        signUpButton = new Button("Choose to sign up");
//        loginButton.addStyleName("redDottedButton");

        horizontalLayout.addComponents(loginButton, signUpButton);
        verticalLayout = new VerticalLayout();

        login();
        singUp();

        root.addComponents(horizontalLayout, verticalLayout);
        setContent(root);
    }

    private void singUp() {
        signUpButton.addClickListener(clickEvent -> {
            verticalLayout.removeAllComponents();
            TextField loginTextField = new TextField("Write login: ");
            TextField emailTextField = new TextField("Write email: ");
            TextField nameTextField = new TextField("Write name: ");
            PasswordField passwordField = new PasswordField("Write password: ");
            Button login = new Button("Sign up");
            verticalLayout.addComponents(loginTextField, emailTextField, nameTextField, passwordField, login);

            login.addClickListener(event -> {
                String loginString = loginTextField.getValue();
                String emailString = emailTextField.getValue();
                String nameString = nameTextField.getValue();
                String passwordString = passwordField.getValue();

                if (loginString.length() > 4) {
                    if (EmailValidator.getInstance(true).isValid(emailString)) {
                        if (nameString.length() > 4) {
                            if (passwordString.length() > 6 && passwordString.length() < 20) {
                                if (validatePassword(passwordString)) {
                                    if (!userRepository.findAllLogins().contains(loginString)) {
                                        if (!userRepository.findAllEmails().contains(emailString)) {
                                            if (!userRepository.findAllNames().contains(nameString)) {
                                                User user = new User(0L, loginString, Hashing.sha512().hashString(passwordString, StandardCharsets.UTF_8).toString()
                                                        , emailString, nameString);
                                                userRepository.save(user);
                                                Notification.show("Successfully signed up!",
                                                        "",
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } else {
                                                Notification.show("This name already exists!", "",
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            }
                                        } else {
                                            Notification.show("This email already exists!", "",
                                                    Notification.Type.HUMANIZED_MESSAGE);
                                        }
                                    } else {
                                        Notification.show("This login already exists!", "",
                                                Notification.Type.HUMANIZED_MESSAGE);
                                    }
                                } else
                                    Notification.show("Password has to contain at least one digit," +
                                                    " one lowercase and one uppercase character!", "",
                                            Notification.Type.HUMANIZED_MESSAGE);
                            } else
                                Notification.show("Password has to have between 6 and 20 characters!", "",
                                        Notification.Type.HUMANIZED_MESSAGE);
                        } else
                            Notification.show("Name has to have more than 4 characters!", "",
                                    Notification.Type.HUMANIZED_MESSAGE);
                    } else
                        Notification.show("Email is not correct!", "",
                                Notification.Type.HUMANIZED_MESSAGE);
                } else
                    Notification.show("Login has to have more than 4 characters!", "",
                            Notification.Type.HUMANIZED_MESSAGE);
            });
        });
    }

    private void login() {
        loginButton.addClickListener(clickEvent -> {
            verticalLayout.removeAllComponents();
            TextField loginTextField = new TextField("Write login: ");
            PasswordField passwordField = new PasswordField("Write password: ");
            Button login = new Button("Login");
            verticalLayout.addComponents(loginTextField, passwordField, login);

            login.addClickListener(event -> {
                Optional<User> user = userRepository.findByLogin(loginTextField.getValue());
                if (user.isPresent()) {
                    if (user.get().getPassword().equals(Hashing.sha512().hashString(passwordField.getValue(), StandardCharsets.UTF_8).toString())) {
                        Notification.show("Login successful!",
                                "",
                                Notification.Type.HUMANIZED_MESSAGE);
                        root.removeAllComponents();
                        root.addComponent(new LoggedUI(user.get(), userRepository, projectRepository, projectParticipationRepository, taskRepository, sprintRepository));

                        Button signOut = new Button("Sign out");
                        signOut.addClickListener(event1 -> {
                            root.removeAllComponents();
                            init(null);
                            loginButton.click();
                        });

                        root.addComponent(signOut);
                    } else {
                        Notification.show("Wrong password!", "",
                                Notification.Type.HUMANIZED_MESSAGE);
                    }
                } else {
                    Notification.show("This login does not exist!", "",
                            Notification.Type.HUMANIZED_MESSAGE);
                }
            });
        });
    }

    private boolean validatePassword(String passwordString) {
        final String PASSWORD_PATTERN =
                "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(passwordString);
        return matcher.matches();
    }

}
