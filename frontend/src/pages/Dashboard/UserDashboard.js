import React, { useEffect, useState } from "react";
import Navbar from "../../components/Navbar";
import axios from "axios";
import styles from "./Dashboard.module.css";
import { authHeader } from "../../utils/authHeader";
import { useNavigate } from "react-router-dom";
import { FaShieldAlt, FaFileSignature, FaCalendarAlt } from "react-icons/fa";

const UserDashboard = () => {
  const navigate = useNavigate();
  const [policies, setPolicies] = useState([]);
  const [claims, setClaims] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const API_BASE = "http://localhost:8080/api";
  const username = localStorage.getItem("username");
  const employeeId = localStorage.getItem("employeeId");
// Use userId for API calls, appending it to FormData or as request params

  const fetchData = async () => {
    try {
      // Step 1: Fetch employee details by username
      const empRes = await axios.get(`${API_BASE}/employees/by-username/${username}`, {
        headers: authHeader(),
      });
      const employeeId = empRes.data.id;

      // Step 2: Fetch employee-specific data
      const [policyRes, claimRes, appRes] = await Promise.all([
        axios.get(`${API_BASE}/policies/employee/${employeeId}`, { headers: authHeader() }),
        axios.get(`${API_BASE}/claims/employee/${employeeId}`, { headers: authHeader() }),
        axios.get(`${API_BASE}/appointments/employee/${employeeId}`, { headers: authHeader() }),
      ]);

      setPolicies(policyRes.data || []);
      setClaims(claimRes.data || []);
      setAppointments(appRes.data || []);
    } catch (err) {
      console.error("Error fetching dashboard data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  if (loading) {
    return (
      <div className={styles.dashboardWrapper}>
        <Navbar />
        <div className={styles.dashboardContainer}>
          <h2 className={styles.subtitle}>Loading your dashboard...</h2>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.dashboardWrapper}>
      <Navbar />
      <div className={styles.dashboardContainer}>
        <h1 className={styles.dashboardTitle}>Welcome to Your Dashboard</h1>
        <p className={styles.subtitle}>
          Manage your policies, track claims, and check your appointments with ease.
        </p>

        <div className={styles.cardsContainer}>
          {/* My Policies */}
          <div className={`${styles.card} ${styles.cardBlue}`}>
            <div className={styles.iconWrapper}>
              <FaShieldAlt className={styles.icon} />
            </div>
            <h2>My Policies</h2>
            <p className={styles.count}>{policies.length}</p>
            <button
              className={styles.btn}
              onClick={() => navigate("/employee-policies")}
            >
              View Policies
            </button>
          </div>

          {/* My Claims */}
          <div className={`${styles.card} ${styles.cardGreen}`}>
            <div className={styles.iconWrapper}>
              <FaFileSignature className={styles.icon} />
            </div>
            <h2>My Claims</h2>
            <p className={styles.count}>{claims.length}</p>
            <button
              className={styles.btn}
              onClick={() => navigate("/employee-claims")}
            >
              View Claims
            </button>
          </div>

          {/* My Appointments */}
          <div className={`${styles.card} ${styles.cardPurple}`}>
            <div className={styles.iconWrapper}>
              <FaCalendarAlt className={styles.icon} />
            </div>
            <h2>My Appointments</h2>
            <p className={styles.count}>{appointments.length}</p>
            <button className={styles.btn}>View Appointments</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
