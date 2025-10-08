import React from "react";
import { useNavigate } from "react-router-dom";
import "./HomePage.css";
import { FaShieldAlt, FaFileSignature, FaCalendarAlt, FaUsers } from "react-icons/fa";
import Chatbot from "../../components/Chatbot"; // Import Chatbot component

const HomePage = () => {
  const navigate = useNavigate();

  return (
    <div className="home-wrapper">

      <header className="hero-section">
        <div className="hero-content">
          <h1>Welcome to InsurAI</h1>
          <p>Your intelligent insurance assistant. Manage policies, claims, and appointments with ease.</p>
          <div className="hero-buttons">
            <button onClick={() => navigate("/login")} className="btn-primary">
              Login
            </button>
            <button onClick={() => navigate("/signup")} className="btn-secondary">
              Sign Up
            </button>
          </div>
        </div>
        <div className="hero-image">
          <img src="/assets/insurance_illustration.svg" alt="Insurance Illustration" />
        </div>
      </header>

      <section className="features-section">
        <h2>Our Features</h2>
        <div className="features-cards">
          <div className="feature-card">
            <FaShieldAlt className="feature-icon" />
            <h3>Manage Policies</h3>
            <p>View, track, and renew your insurance policies seamlessly.</p>
          </div>
          <div className="feature-card">
            <FaFileSignature className="feature-icon" />
            <h3>Claims Tracking</h3>
            <p>Submit and monitor claims quickly with smart notifications.</p>
          </div>
          <div className="feature-card">
            <FaCalendarAlt className="feature-icon" />
            <h3>Appointments</h3>
            <p>Book meetings with agents and stay updated on your schedule.</p>
          </div>
          <div className="feature-card">
            <FaUsers className="feature-icon" />
            <h3>Customer Support</h3>
            <p>Get 24/7 AI-powered assistance for all your insurance needs.</p>
          </div>
        </div>
      </section>

      {/* Chatbot Floating Component */}
      <Chatbot />

    </div>
  );
};

export default HomePage;
