import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class JavaBeans_courses {
    public static void main(String[] args) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT)");
            con.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id TEXT,
                    program_id TEXT,
                    code TEXT PRIMARY KEY,
                    name TEXT,
                    category TEXT,
                    type TEXT,
                    prereq TEXT,
                    coreq TEXT,
                    progression TEXT,
                    course_plan TEXT
                )
            """);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new ModernLogin();
    }
}

class ModernLogin extends Frame {
    TextField user, pass;
    Label status;

    ModernLogin() {
        setTitle("Login");
        setSize(300, 200);
        setLayout(null);
        setBackground(Color.WHITE);

        Label title = new Label("Course Login", Label.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setBounds(70, 30, 160, 20);
        add(title);

        Label userLabel = new Label("Username:");
        Label passLabel = new Label("Password:");
        userLabel.setBounds(40, 60, 70, 20);
        passLabel.setBounds(40, 90, 70, 20);
        add(userLabel);
        add(passLabel);

        user = new TextField();
        pass = new TextField();
        pass.setEchoChar('*');
        user.setBounds(120, 60, 120, 22);
        pass.setBounds(120, 90, 120, 22);
        add(user);
        add(pass);

        Button login = new Button("Login");
        Button register = new Button("Register");
        login.setBounds(40, 130, 80, 25);
        register.setBounds(160, 130, 80, 25);
        add(login);
        add(register);

        status = new Label("", Label.CENTER);
        status.setBounds(40, 160, 200, 20);
        status.setForeground(Color.RED);
        add(status);

        login.addActionListener(e -> {
            if (CourseOperations.authenticate(user.getText(), pass.getText())) {
                status.setText("Login Successful");
                dispose();
                new CourseDashboard(user.getText());
            } else {
                status.setText("Invalid Credentials");
            }
        });

        register.addActionListener(e -> {
            CourseOperations.register(user.getText(), pass.getText());
            status.setText("User Registered");
        });

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
}

class CourseDashboard extends Frame implements ActionListener {
    TextField[] fields = new TextField[10];
    TextArea display;
    String[] labels = {"ID", "Program ID", "Code", "Name", "Category", "Type", "Pre-req", "Co-req", "Progression", "Course plan"};
    String originalCode = "";
    String loggedInUser;

    CourseDashboard(String username) {
        this.loggedInUser = username;

        setTitle("Course Dashboard");
        setSize(780, 600);
        setLayout(null);
        setBackground(Color.WHITE);

        Label header = new Label("Course Management", Label.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setBounds(20, 40, 740, 30);
        add(header);

        Label userInfo = new Label("Logged in as: " + username);
        userInfo.setBounds(40, 70, 300, 20);
        add(userInfo);

        Panel formPanel = new Panel();
        formPanel.setLayout(new GridLayout(10, 2, 8, 6));
        formPanel.setBounds(40, 100, 700, 280);
        formPanel.setBackground(Color.LIGHT_GRAY);
        for (int i = 0; i < labels.length; i++) {
            Label l = new Label(labels[i]);
            TextField tf = new TextField();
            fields[i] = tf;
            formPanel.add(l);
            formPanel.add(tf);
        }
        add(formPanel);

        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new GridLayout(1, 7, 10, 0));
        buttonPanel.setBounds(40, 390, 700, 30);
        String[] btns = {"Create", "Update", "Delete", "View All", "Retrive", "Get by Program", "Reset"};
        for (String btn : btns) {
            Button b = new Button(btn);
            b.addActionListener(this);
            buttonPanel.add(b);
        }
        add(buttonPanel);

        display = new TextArea();
        display.setBounds(40, 430, 700, 120);
        display.setEditable(false);
        display.setBackground(new Color(245, 245, 245));
        display.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(display);

        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        String[] data = new String[10];
        for (int i = 0; i < 10; i++) {
            data[i] = fields[i].getText().trim();  // Trim input
            System.out.println("Field " + i + ": " + data[i]);  // Debug log
        }

        switch (cmd) {
            case "Create" -> {
                CourseOperations.JavaBeans_courses_create(data);
                display.setText("✅ Course created!");
            }
            case "Update" -> {
                CourseOperations.JavaBeans_courses_update(data, originalCode);
                display.setText("✅ Course updated!");
            }
            case "Delete" -> {
                CourseOperations.JavaBeans_courses_delete(data[2]);
                display.setText("🗑 Course deleted: " + data[2]);
            }
            case "View All" -> {
                var all = CourseOperations.viewAll();
                display.setText("");
                for (String[] row : all) display.append(String.join(" | ", row) + "\n");
            }
            case "Retrive" -> {
                if (!data[0].isEmpty()) {
                    var row = CourseOperations.retrieveById(data[0]);
                    if (row != null) {
                        for (int i = 0; i < 10; i++) fields[i].setText(row[i]);
                        originalCode = row[2];
                        display.setText("📦 Retrieved by ID");
                    } else {
                        display.setText("❌ Not found by ID.");
                    }
                } else if (!data[1].isEmpty()) {
                    var rows = CourseOperations.retrieveByProgramId(data[1]);
                    if (!rows.isEmpty()) {
                        String[] row = rows.get(0);
                        for (int i = 0; i < 10; i++) fields[i].setText(row[i]);
                        originalCode = row[2];
                        display.setText("📦 Courses for Program ID:\n");
                        for (String[] r : rows) display.append(String.join(" | ", r) + "\n");
                    } else {
                        display.setText("❌ No courses for Program ID.");
                    }
                } else {
                    display.setText("⚠ Please enter ID or Program ID");
                }
            }
            case "Get by Program" -> {
                var rows = CourseOperations.retrieveByProgramId(data[1]);
                display.setText("");
                for (String[] row : rows) display.append(String.join(" | ", row) + "\n");
            }
            case "Reset" -> {
                for (TextField field : fields) field.setText("");
                display.setText("🔄 Reset");
                originalCode = "";
            }
        }
    }
}

class CourseOperations {
    static void register(String u, String p) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("INSERT OR IGNORE INTO users VALUES (?, ?)");
            ps.setString(1, u);
            ps.setString(2, p);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean authenticate(String u, String p) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, u);
            ps.setString(2, p);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void JavaBeans_courses_create(String[] d) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("INSERT OR REPLACE INTO courses VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (int i = 0; i < 10; i++) ps.setString(i + 1, d[i]);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void JavaBeans_courses_update(String[] d, String oldCode) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE courses SET id=?, program_id=?, code=?, name=?, category=?, type=?, prereq=?, coreq=?, progression=?, course_plan=? WHERE code=?"
            );
            for (int i = 0; i < 10; i++) ps.setString(i + 1, d[i]);
            ps.setString(11, oldCode);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void JavaBeans_courses_delete(String code) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM courses WHERE code=?");
            ps.setString(1, code);
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static ArrayList<String[]> viewAll() {
        ArrayList<String[]> list = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM courses");
            while (rs.next()) {
                String[] row = new String[10];
                for (int i = 0; i < 10; i++) row[i] = rs.getString(i + 1);
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    static String[] retrieveById(String id) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM courses WHERE id=?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String[] row = new String[10];
                for (int i = 0; i < 10; i++) row[i] = rs.getString(i + 1);
                return row;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ArrayList<String[]> retrieveByProgramId(String programId) {
        ArrayList<String[]> list = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:courses.db")) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM courses WHERE program_id=?");
            ps.setString(1, programId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String[] row = new String[10];
                for (int i = 0; i < 10; i++) row[i] = rs.getString(i + 1);
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
