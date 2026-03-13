/* ============================================
   Event Manager - Main JavaScript
   ============================================ */

document.addEventListener('DOMContentLoaded', function () {

    // ============================================
    // Sidebar Toggle (Mobile)
    // ============================================
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');

    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function () {
            sidebar.classList.toggle('show');
            if (overlay) overlay.classList.toggle('active');
        });
    }

    if (overlay) {
        overlay.addEventListener('click', function () {
            sidebar.classList.remove('show');
            overlay.classList.remove('active');
        });
    }

    // ============================================
    // Toggle Password Visibility
    // ============================================
    document.querySelectorAll('.toggle-password').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (!input) return;

            const icon = this.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

    // ============================================
    // Form Validation - Login
    // ============================================
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function (e) {
            let valid = true;
            const username = document.getElementById('username');
            const password = document.getElementById('password');

            // Reset
            username.classList.remove('is-invalid');
            password.classList.remove('is-invalid');

            if (!username.value.trim()) {
                username.classList.add('is-invalid');
                valid = false;
            }

            if (!password.value) {
                password.classList.add('is-invalid');
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
            }
        });
    }

    // ============================================
    // Form Validation - Registro
    // ============================================
    const registroForm = document.getElementById('registroForm');
    if (registroForm) {
        registroForm.addEventListener('submit', function (e) {
            let valid = true;
            const nombre = document.getElementById('nombre');
            const username = document.getElementById('username');
            const password = document.getElementById('password');
            const confirmPassword = document.getElementById('confirmPassword');

            // Reset
            [nombre, username, password, confirmPassword].forEach(function (el) {
                el.classList.remove('is-invalid', 'is-valid');
            });

            if (!nombre.value.trim()) {
                nombre.classList.add('is-invalid');
                valid = false;
            }

            if (!username.value.trim() || username.value.trim().length < 3) {
                username.classList.add('is-invalid');
                valid = false;
            }

            if (!password.value || password.value.length < 6) {
                password.classList.add('is-invalid');
                valid = false;
            }

            if (password.value !== confirmPassword.value) {
                confirmPassword.classList.add('is-invalid');
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
            }
        });

        // Real-time password match check
        const confirmPassword = document.getElementById('confirmPassword');
        const password = document.getElementById('password');
        if (confirmPassword && password) {
            confirmPassword.addEventListener('input', function () {
                if (this.value && this.value !== password.value) {
                    this.classList.add('is-invalid');
                    this.classList.remove('is-valid');
                } else if (this.value && this.value === password.value) {
                    this.classList.remove('is-invalid');
                    this.classList.add('is-valid');
                } else {
                    this.classList.remove('is-invalid', 'is-valid');
                }
            });
        }
    }

    // ============================================
    // Generic Fetch API Helper
    // ============================================
    window.apiFetch = async function (url, options = {}) {
        const defaults = {
            headers: {
                'Content-Type': 'application/json',
            },
        };
        const config = { ...defaults, ...options };

        try {
            const response = await fetch(url, config);
            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.message || 'Error en la solicitud');
            }
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    };
});
