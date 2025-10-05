import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../../services/authService";
import styles from "./Login.module.css";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    try {
      const response = await login(username, password);
      console.log("Login response:", response.data);

      // Save auth/token and info
      localStorage.setItem("token", response.data.token);
      localStorage.setItem("username", response.data.username);
      localStorage.setItem("role", response.data.role);

      // Always save userId (mandatory for all roles)
      if (response.data.userId) {
        localStorage.setItem("userId", response.data.userId);
      } else {
        // Defensive guard
        alert("Login response did not include userId. Contact admin.");
        return;
      }

      // Save employeeId for employees (for claim operations)
      if (response.data.role === "EMPLOYEE" && response.data.employeeId) {
        localStorage.setItem("employeeId", response.data.employeeId);
      } else {
        localStorage.removeItem("employeeId");
      }

      // Redirect based on role
      switch (response.data.role) {
        case "ADMIN":
          navigate("/admin-dashboard");
          break;
        case "EMPLOYEE":
          navigate("/user-dashboard");
          break;
        case "AGENT":
          navigate("/agent-dashboard");
          break;
        default:
          setError("Invalid role detected. Contact support.");
      }
    } catch (err) {
      if (err.response && err.response.status === 401) {
        setError("Invalid username or password");
      } else {
        setError("Something went wrong. Try again.");
      }
    }
  };

  return (
    <div className={styles.container}>
      <h2>Login</h2>
      {error && <p className={styles.error}>{error}</p>}
      <form onSubmit={handleLogin}>
        <div className={styles.inputGroup}>
          <input
            type="text"
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className={styles.inputGroup}>
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className={styles.btn}>
          Login
        </button>
      </form>
      <p>
        Don't have an account? <Link to="/signup">Signup here</Link>
      </p>
    </div>
  );
};

export default Login;
