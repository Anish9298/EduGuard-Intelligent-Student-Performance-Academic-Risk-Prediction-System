// ============================================================
// HELPERS
// ============================================================

async function api(url, options = {}) {

    const response = await fetch(url, options);

    if (response.status === 401 &&
        !url.includes("api/login")) {

        window.location.href = "login.html";

        throw new Error("Not logged in");
    }

    const data =
        await response.json().catch(() => ({}));

    if (!response.ok) {

        throw new Error(
            data.message ||
            data.error ||
            "Request failed"
        );
    }

    return data;
}


function postJson(url, data) {

    return api(url, {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(data)
    });
}


function showMsg(message) {

    const element =
        document.getElementById("msg");

    if (element) {
        element.textContent = message || "";
    }
}


function logout() {

    api("api/logout", {
        method: "POST"
    }).finally(() => {

        window.location.href =
            "login.html";
    });
}


function percentage(value, total) {

    if (!total) {
        return "0.0";
    }

    return ((value * 100) / total).toFixed(1);
}


function escapeHtml(value) {

    return String(value ?? "").replace(
        /[&<>"']/g,
        function (character) {

            const replacements = {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#39;"
            };

            return replacements[character];
        }
    );
}


// ============================================================
// LOGIN
// ============================================================

function initLogin() {

    const loginForm =
        document.getElementById("loginForm");

    if (!loginForm) {
        return;
    }


    loginForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            const username =
                document.getElementById("username")
                    .value.trim();

            const password =
                document.getElementById("password")
                    .value;


            try {

                const data =
                    await api(
                        "api/login",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded"
                            },

                            body: new URLSearchParams({
                                username: username,
                                password: password
                            })
                        }
                    );


                if (data.success) {

                    if (data.role === "ADMIN") {

                        window.location.href =
                            "dashboard.html";

                    } else {

                        window.location.href =
                            "student.html";
                    }

                } else {

                    showMsg(
                        data.message ||
                        "Invalid username or password"
                    );
                }


            } catch (error) {

                showMsg(
                    error.message ||
                    "Invalid username or password"
                );
            }
        }
    );
}


// ============================================================
// FORGOT PASSWORD
// ============================================================

function initForgotPassword() {

    const showForgot =
        document.getElementById("showForgot");

    const showLogin =
        document.getElementById("showLogin");

    const loginSection =
        document.getElementById("loginSection");

    const forgotSection =
        document.getElementById("forgotSection");

    const forgotForm =
        document.getElementById("forgotForm");


    if (!showForgot ||
        !showLogin ||
        !loginSection ||
        !forgotSection ||
        !forgotForm) {

        return;
    }


    // SHOW FORGOT PASSWORD

    showForgot.addEventListener(
        "click",
        function () {

            loginSection.style.display =
                "none";

            forgotSection.style.display =
                "block";

            document.getElementById(
                "forgotUsername"
            ).focus();
        }
    );


    // BACK TO LOGIN

    showLogin.addEventListener(
        "click",
        function () {

            forgotSection.style.display =
                "none";

            loginSection.style.display =
                "block";

            document.getElementById(
                "username"
            ).focus();
        }
    );


    // RESET PASSWORD

    forgotForm.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            const username =
                document.getElementById(
                    "forgotUsername"
                ).value.trim();


            const newPassword =
                document.getElementById(
                    "forgotPassword"
                ).value;


            const confirmPassword =
                document.getElementById(
                    "forgotConfirmPassword"
                ).value;


            const message =
                document.getElementById(
                    "forgotMsg"
                );


            // PASSWORD LENGTH

            if (newPassword.length < 6) {

                message.textContent =
                    "Password must contain at least 6 characters.";

                message.className =
                    "error";

                return;
            }


            // PASSWORD MATCH

            if (newPassword !== confirmPassword) {

                message.textContent =
                    "Passwords do not match.";

                message.className =
                    "error";

                return;
            }


            try {

                const response =
                    await fetch(
                        "api/forgot-password",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/x-www-form-urlencoded"
                            },

                            body: new URLSearchParams({
                                username: username,
                                newPassword: newPassword,
                                confirmPassword: confirmPassword
                            })
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.message ||
                        "Password reset failed"
                    );
                }


                message.textContent =
                    data.message ||
                    "Password reset successfully.";

                message.className =
                    "success";


                forgotForm.reset();


                // RETURN TO LOGIN

                setTimeout(
                    function () {

                        forgotSection.style.display =
                            "none";

                        loginSection.style.display =
                            "block";

                        document.getElementById(
                            "username"
                        ).value = username;

                        document.getElementById(
                            "password"
                        ).focus();

                    },
                    1500
                );


            } catch (error) {

                message.textContent =
                    error.message ||
                    "Password reset failed.";

                message.className =
                    "error";
            }
        }
    );
}


// ============================================================
// STUDENTS
// ============================================================

async function loadStudents() {

    const students =
        await api("api/students");


    const table =
        document.getElementById(
            "studentTable"
        );


    if (!table) {
        return;
    }


    table.innerHTML =
        students.map(
            function (student) {

                return `
                    <tr>

                        <td>${student.id}</td>

                        <td>
                            ${escapeHtml(student.rollNo)}
                        </td>

                        <td>
                            ${escapeHtml(student.name)}
                        </td>

                        <td>
                            ${escapeHtml(student.course)}
                        </td>

                        <td>
                            ${student.semester}
                        </td>

                        <td>

                            <button
                                class="danger"
                                onclick="deleteStudent(${student.id})">

                                Delete

                            </button>

                        </td>

                    </tr>
                `;
            }
        ).join("");
}


async function deleteStudent(id) {

    if (!confirm("Delete this student?")) {
        return;
    }


    try {

        await api(
            "api/students?id=" + id,
            {
                method: "DELETE"
            }
        );


        await loadStudents();


    } catch (error) {

        showMsg(error.message);
    }
}


function initStudent() {

    const form =
        document.getElementById(
            "studentForm"
        );


    if (!form) {
        return;
    }


    form.addEventListener(
        "submit",
        async function (event) {

            event.preventDefault();


            try {

                await postJson(
                    "api/students",
                    {

                        rollNo:
                            document.getElementById(
                                "rollNo"
                            ).value.trim(),

                        name:
                            document.getElementById(
                                "name"
                            ).value.trim(),

                        course:
                            document.getElementById(
                                "course"
                            ).value.trim(),

                        semester:
                            parseInt(
                                document.getElementById(
                                    "semester"
                                ).value
                            )
                    }
                );


                form.reset();


                await loadStudents();


            } catch (error) {

                showMsg(error.message);
            }
        }
    );


    loadStudents();
}


// ============================================================
// ACADEMIC
// ============================================================

function currentStudentId() {

    const select =
        document.getElementById(
            "studentSelect"
        );


    if (!select) {
        return 0;
    }


    return parseInt(select.value);
}


async function loadAcademic() {

    const studentId =
        currentStudentId();


    if (!studentId) {
        return;
    }


    // ATTENDANCE

    const attendance =
        await api(
            "api/attendance?studentId=" +
            studentId
        );


    const attendanceTable =
        document.getElementById(
            "attTable"
        );


    if (attendanceTable) {

        attendanceTable.innerHTML =
            attendance.map(
                function (item) {

                    return `
                        <tr>

                            <td>
                                ${escapeHtml(item.subject)}
                            </td>

                            <td>
                                ${item.attendedClasses}
                            </td>

                            <td>
                                ${item.totalClasses}
                            </td>

                            <td>
                                ${percentage(
                        item.attendedClasses,
                        item.totalClasses
                    )}%
                            </td>

                            <td>

                                <button
                                    class="danger"
                                    onclick="deleteRecord(
                                        'attendance',
                                        ${item.id}
                                    )">

                                    X

                                </button>

                            </td>

                        </tr>
                    `;
                }
            ).join("");
    }


    // MARKS

    const marks =
        await api(
            "api/marks?studentId=" +
            studentId
        );


    const marksTable =
        document.getElementById(
            "marksTable"
        );


    if (marksTable) {

        marksTable.innerHTML =
            marks.map(
                function (item) {

                    return `
                        <tr>

                            <td>
                                ${escapeHtml(item.subject)}
                            </td>

                            <td>
                                ${escapeHtml(item.examType)}
                            </td>

                            <td>
                                ${item.marksObtained}/${item.maxMarks}
                            </td>

                            <td>
                                ${percentage(
                        item.marksObtained,
                        item.maxMarks
                    )}%
                            </td>

                            <td>

                                <button
                                    class="danger"
                                    onclick="deleteRecord(
                                        'marks',
                                        ${item.id}
                                    )">

                                    X

                                </button>

                            </td>

                        </tr>
                    `;
                }
            ).join("");
    }
}


async function deleteRecord(type, id) {

    try {

        await api(
            "api/" +
            type +
            "?id=" +
            id,
            {
                method: "DELETE"
            }
        );


        await loadAcademic();


    } catch (error) {

        showMsg(error.message);
    }
}


async function initAcademic() {

    const students =
        await api("api/students");


    const select =
        document.getElementById(
            "studentSelect"
        );


    if (!select) {
        return;
    }


    if (students.length === 0) {

        select.innerHTML =
            `<option value="">
                No students yet
            </option>`;

        return;
    }


    select.innerHTML =
        students.map(
            function (student) {

                return `
                    <option value="${student.id}">
                        ${escapeHtml(student.rollNo)}
                        -
                        ${escapeHtml(student.name)}
                    </option>
                `;
            }
        ).join("");


    select.addEventListener(
        "change",
        loadAcademic
    );


    // ATTENDANCE FORM

    const attendanceForm =
        document.getElementById(
            "attForm"
        );


    if (attendanceForm) {

        attendanceForm.addEventListener(
            "submit",
            async function (event) {

                event.preventDefault();


                const studentId =
                    currentStudentId();


                if (!studentId) {

                    showMsg(
                        "Please select a student first."
                    );

                    return;
                }


                try {

                    await postJson(
                        "api/attendance",
                        {

                            student: {
                                id: studentId
                            },

                            subject:
                                document.getElementById(
                                    "attSubject"
                                ).value.trim(),

                            totalClasses:
                                parseInt(
                                    document.getElementById(
                                        "attTotal"
                                    ).value
                                ),

                            attendedClasses:
                                parseInt(
                                    document.getElementById(
                                        "attAttended"
                                    ).value
                                )
                        }
                    );


                    attendanceForm.reset();


                    await loadAcademic();


                } catch (error) {

                    showMsg(error.message);
                }
            }
        );
    }


    // MARKS FORM

    const marksForm =
        document.getElementById(
            "marksForm"
        );


    if (marksForm) {

        marksForm.addEventListener(
            "submit",
            async function (event) {

                event.preventDefault();


                const studentId =
                    currentStudentId();


                if (!studentId) {

                    showMsg(
                        "Please select a student first."
                    );

                    return;
                }


                try {

                    await postJson(
                        "api/marks",
                        {

                            student: {
                                id: studentId
                            },

                            subject:
                                document.getElementById(
                                    "mSubject"
                                ).value.trim(),

                            examType:
                            document.getElementById(
                                "mExam"
                            ).value,

                            marksObtained:
                                parseFloat(
                                    document.getElementById(
                                        "mObtained"
                                    ).value
                                ),

                            maxMarks:
                                parseFloat(
                                    document.getElementById(
                                        "mMax"
                                    ).value
                                )
                        }
                    );


                    marksForm.reset();


                    await loadAcademic();


                } catch (error) {

                    showMsg(error.message);
                }
            }
        );
    }


    await loadAcademic();
}


// ============================================================
// DASHBOARD
// ============================================================

let riskChart = null;


async function loadDashboard() {

    try {

        const students =
            await api("api/students");


        const results =
            await Promise.all(
                students.map(
                    async function (student) {

                        const risk =
                            await api(
                                "api/risk?studentId=" +
                                student.id
                            );

                        return {
                            student: student,
                            risk: risk
                        };
                    }
                )
            );


        const counts = {

            LOW: 0,

            MEDIUM: 0,

            HIGH: 0
        };


        results.forEach(
            function (result) {

                const level =
                    result.risk.riskLevel;


                if (counts[level] !== undefined) {

                    counts[level]++;
                }
            }
        );


        const totalCount =
            document.getElementById(
                "totalCount"
            );

        const lowCount =
            document.getElementById(
                "lowCount"
            );

        const mediumCount =
            document.getElementById(
                "medCount"
            );

        const highCount =
            document.getElementById(
                "highCount"
            );


        if (totalCount) {
            totalCount.textContent =
                students.length;
        }

        if (lowCount) {
            lowCount.textContent =
                counts.LOW;
        }

        if (mediumCount) {
            mediumCount.textContent =
                counts.MEDIUM;
        }

        if (highCount) {
            highCount.textContent =
                counts.HIGH;
        }


        // SORT BY RISK

        results.sort(
            function (a, b) {

                return b.risk.riskScore -
                    a.risk.riskScore;
            }
        );


        const riskTable =
            document.getElementById(
                "riskTable"
            );


        if (riskTable) {

            riskTable.innerHTML =
                results.map(
                    function (result) {

                        const student =
                            result.student;

                        const risk =
                            result.risk;


                        return `
                            <tr>

                                <td>
                                    ${escapeHtml(
                            student.rollNo
                        )}
                                </td>

                                <td>
                                    ${escapeHtml(
                            student.name
                        )}
                                </td>

                                <td>
                                    ${risk.riskScore}
                                </td>

                                <td>

                                    <span
                                        class="badge ${escapeHtml(
                            risk.riskLevel
                        )}">

                                        ${escapeHtml(
                            risk.riskLevel
                        )}

                                    </span>

                                </td>

                                <td>
                                    ${escapeHtml(
                            risk.reason
                        )}
                                </td>

                            </tr>
                        `;
                    }
                ).join("");
        }


        // CHART

        const canvas =
            document.getElementById(
                "riskChart"
            );


        if (!canvas) {
            return;
        }


        const chartData = [

            counts.LOW,

            counts.MEDIUM,

            counts.HIGH
        ];


        if (riskChart) {

            riskChart.data.datasets[0].data =
                chartData;

            riskChart.update();

        } else {

            riskChart =
                new Chart(
                    canvas,
                    {
                        type: "doughnut",

                        data: {

                            labels: [
                                "Low",
                                "Medium",
                                "High"
                            ],

                            datasets: [
                                {
                                    data: chartData,

                                    backgroundColor: [
                                        "#2f9e44",
                                        "#f08c00",
                                        "#e03131"
                                    ]
                                }
                            ]
                        }
                    }
                );
        }


    } catch (error) {

        showMsg(error.message);
    }
}


function initDashboard() {

    const refreshButton =
        document.getElementById(
            "refreshBtn"
        );


    if (refreshButton) {

        refreshButton.addEventListener(
            "click",
            loadDashboard
        );
    }


    loadDashboard();
}


// ============================================================
// START APPLICATION
// ============================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const page =
            document.body.dataset.page;


        if (page === "login") {

            initLogin();

            initForgotPassword();

        } else if (page === "student") {

            initStudent();

        } else if (page === "academic") {

            initAcademic();

        } else if (page === "dashboard") {

            initDashboard();
        }
    }
);