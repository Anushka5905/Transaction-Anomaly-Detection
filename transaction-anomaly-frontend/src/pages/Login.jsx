import { useState } from "react";
import axios from "axios";

const API = "http://localhost:8080/api";

function Login({ onLogin }) {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const handleSubmit = async (event) => {

    event.preventDefault();
    setMessage("");

    try {

      const response = await axios.post(
        `${API}/auth/login`,
        {
          email,
          password
        }
      );

      const data = response.data;

      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify({
        name: data.name,
        email: data.email,
        role: data.role
      }));

      onLogin(data);

    } catch (error) {

      console.error("Login error:", error);

      setMessage(
        error.response?.data?.message ||
        "Invalid email or password"
      );
    }
  };

  return (
    <div className="login-page">

      <div className="login-card">

        <h1>Transaction Anomaly System</h1>

        <p className="login-subtitle">
          Sign in to continue
        </p>

        <form onSubmit={handleSubmit}>

          <label>Email</label>

          <input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <label>Password</label>

          <input
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <button type="submit">
            Login
          </button>

        </form>

        {message && (
          <p className="login-message">
            {message}
          </p>
        )}

      </div>

    </div>
  );
}

export default Login;