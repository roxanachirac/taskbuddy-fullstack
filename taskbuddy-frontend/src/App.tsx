import { useEffect, useState, useCallback } from 'react';

interface Task {
    id: number;
    title: string;
    description: string;
    completed: boolean;
}

interface PrioritizedTaskItem {
    id: number;
    title: string;
    priorityOrder: number;
    justification: string;
}

function App() {
    // Stări pentru Autentificare
    const [user, setUser] = useState<string | null>(localStorage.getItem('tb_username'));
    const [password, setPassword] = useState<string | null>(localStorage.getItem('tb_password'));
    const [isRegistering, setIsRegistering] = useState(false);
    const [authUsername, setAuthUsername] = useState('');
    const [authPassword, setAuthPassword] = useState('');
    const [authError, setAuthError] = useState('');

    // Stări pentru Task-uri și AI
    const [tasks, setTasks] = useState<Task[]>([]);
    const [loading, setLoading] = useState<boolean>(!!localStorage.getItem('tb_username'));
    const [aiLoadingId, setAiLoadingId] = useState<number | null>(null);
    const [prioritizing, setPrioritizing] = useState(false);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [prioritizedList, setPrioritizedList] = useState<PrioritizedTaskItem[]>([]);

    // Helper pentru a genera Header-ul de Basic Authentication
    const getAuthHeader = useCallback((): Record<string, string> => {
        if (!user || !password) return {};
        return { 'Authorization': 'Basic ' + btoa(`${user}:${password}`) };
    }, [user, password]);

    const handleLogout = () => {
        localStorage.removeItem('tb_username');
        localStorage.removeItem('tb_password');
        setUser(null);
        setPassword(null);
        setTasks([]);
        setPrioritizedList([]);
    };

    const fetchTasks = useCallback(() => {
        if (!user || !password) return;

        fetch('/api/tasks', {
            headers: { ...getAuthHeader() }
        })
            .then((res) => {
                if (res.status === 401) throw new Error("Neautorizat");
                return res.json();
            })
            .then((data) => {
                setTasks(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Eroare:", err);
                setLoading(false);
                handleLogout();
            });
    }, [user, password, getAuthHeader]);

    useEffect(() => {
        if (user && password) {
            fetchTasks();
        }
    }, [user, password, fetchTasks]);

    const handleAuthSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setAuthError('');
        if (!authUsername.trim() || !authPassword.trim()) return;

        const url = isRegistering ? '/api/auth/register' : '/api/auth/login';
        const headers: HeadersInit = isRegistering
            ? { 'Content-Type': 'application/json' }
            : { 'Authorization': 'Basic ' + btoa(`${authUsername}:${authPassword}`) };

        const body = isRegistering ? JSON.stringify({ username: authUsername, password: authPassword }) : undefined;

        fetch(url, { method: isRegistering ? 'POST' : 'GET', headers, body })
            .then(async (res) => {
                const data = await res.json();
                // 🛠️ MODIFICAT: Citim câmpul .error trimis de CustomAuthenticationEntryPoint sau .message
                if (!res.ok) {
                    throw new Error(data.error || data.message || "A apărut o eroare la autentificare.");
                }
                return data;
            })
            .then(() => {
                if (isRegistering) {
                    alert("Cont creat cu succes! Acum te poți loga.");
                    setIsRegistering(false);
                    setAuthPassword('');
                } else {
                    localStorage.setItem('tb_username', authUsername);
                    localStorage.setItem('tb_password', authPassword);
                    setLoading(true);
                    setUser(authUsername);
                    setPassword(authPassword);
                    setAuthUsername('');
                    setAuthPassword('');
                }
            })
            .catch((err) => setAuthError(err.message));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) return;

        fetch('/api/tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
            body: JSON.stringify({ title, description }),
        })
            .then((res) => res.json())
            .then((data) => {
                setTasks(data);
                setTitle('');
                setDescription('');
            })
            .catch((err) => console.error("Eroare la salvare:", err));
    };

    const handleToggle = (id: number) => {
        fetch(`/api/tasks/${id}/toggle`, {
            method: 'PUT',
            headers: { ...getAuthHeader() }
        })
            .then((res) => res.json())
            .then((data) => setTasks(data))
            .catch((err) => console.error("Eroare la actualizare:", err));
    };

    const handleDelete = (id: number, e: React.MouseEvent) => {
        e.stopPropagation();
        if (window.confirm("Sigur vrei să ștergi acest task?")) {
            fetch(`/api/tasks/${id}`, {
                method: 'DELETE',
                headers: { ...getAuthHeader() }
            })
                .then((res) => res.json())
                .then((data) => setTasks(data))
                .catch((err) => console.error("Eroare la ștergere:", err));
        }
    };

    const handleAiSplit = (taskTitle: string, id: number, e: React.MouseEvent) => {
        e.stopPropagation();
        setAiLoadingId(id);

        fetch(`/api/ai/subtasks?task=${encodeURIComponent(taskTitle)}`, {
            headers: { ...getAuthHeader() }
        })
            .then((res) => res.json())
            .then(() => {
                fetchTasks();
                setAiLoadingId(null);
            })
            .catch((err) => {
                console.error("Eroare AI:", err);
                setAiLoadingId(null);
            });
    };

    const handleAiPrioritize = () => {
        setPrioritizing(true);
        setPrioritizedList([]);

        fetch('/api/ai/prioritize', {
            headers: { ...getAuthHeader() }
        })
            .then((res) => res.json())
            .then((data) => {
                if (data && data.prioritizedTasks) {
                    setPrioritizedList(data.prioritizedTasks);
                }
                setPrioritizing(false);
            })
            .catch((err) => {
                console.error("Eroare prioritizare:", err);
                setPrioritizing(false);
            });
    };

    // --- REDERARE INTERFAȚĂ DE LOGIN / REGISTER ---
    if (!user) {
        return (
            <div style={{ maxWidth: '400px', margin: '100px auto', fontFamily: '"Segoe UI", Roboto, sans-serif', padding: '20px' }}>
                <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                    <h1 style={{ color: '#4F46E5', fontSize: '2.2rem', fontWeight: '800', margin: '0 0 5px 0' }}>TaskBuddy 🚀</h1>
                    <p style={{ color: '#4B5563', margin: 0 }}>Securizat cu Spring Security & Postgres</p>
                </div>
                <div style={{ background: '#fff', border: '1px solid #E5E7EB', padding: '30px', borderRadius: '16px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ margin: '0 0 20px 0', color: '#1F2937', fontSize: '1.25rem' }}>
                        {isRegistering ? 'Crează un cont nou' : 'Autentifică-te'}
                    </h3>
                    {authError && <p style={{ color: '#EF4444', fontSize: '0.9rem', marginBottom: '15px', background: '#FEE2E2', padding: '10px', borderRadius: '8px' }}>⚠️ {authError}</p>}
                    <form onSubmit={handleAuthSubmit}>
                        <div style={{ marginBottom: '14px' }}>
                            <input
                                type="text"
                                placeholder="Utilizator"
                                value={authUsername}
                                onChange={(e) => setAuthUsername(e.target.value)}
                                style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #D1D5DB', boxSizing: 'border-box' }}
                                required
                            />
                        </div>
                        <div style={{ marginBottom: '20px' }}>
                            <input
                                type="password"
                                placeholder="Parolă"
                                value={authPassword}
                                onChange={(e) => setAuthPassword(e.target.value)}
                                style={{ width: '100%', padding: '12px', borderRadius: '10px', border: '1px solid #D1D5DB', boxSizing: 'border-box' }}
                                required
                            />
                        </div>
                        <button type="submit" style={{ background: '#4F46E5', color: 'white', border: 'none', padding: '12px', borderRadius: '10px', fontSize: '1rem', fontWeight: '600', cursor: 'pointer', width: '100%' }}>
                            {isRegistering ? 'Înregistrare' : 'Conectare'}
                        </button>
                    </form>
                    <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.9rem', color: '#6B7280' }}>
                        {isRegistering ? 'Ai deja cont?' : 'Nu ai un cont?'} {' '}
                        <span onClick={() => { setIsRegistering(!isRegistering); setAuthError(''); }} style={{ color: '#4F46E5', cursor: 'pointer', fontWeight: 'bold' }}>
                            {isRegistering ? 'Loghează-te' : 'Înregistrează-te'}
                        </span>
                    </p>
                </div>
            </div>
        );
    }

    // --- REDERARE DASHBOARD PRINCIPAL (UTILIZATOR LOGAT) ---
    return (
        <div style={{ maxWidth: '650px', margin: '50px auto', fontFamily: '"Segoe UI", Roboto, sans-serif', padding: '0 20px' }}>

            {/* BARĂ DE SUS PENTRU LOGOUT */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', background: '#F3F4F6', padding: '10px 20px', borderRadius: '12px' }}>
                <span style={{ color: '#374151', fontWeight: '500' }}>👋 Salut, <strong>{user}</strong>!</span>
                <button onClick={handleLogout} style={{ background: 'none', border: 'none', color: '#EF4444', cursor: 'pointer', fontWeight: '600', fontSize: '0.9rem' }}>Deconectare 🚪</button>
            </div>

            {/* HEADER */}
            <div style={{ textAlign: 'center', marginBottom: '40px' }}>
                <h1 style={{ color: '#4F46E5', fontSize: '2.5rem', fontWeight: '800', margin: '0 0 5px 0' }}>
                    TaskBuddy <span style={{ fontSize: '2rem' }}>🚀</span>
                </h1>
                <p style={{ color: '#4B5563', fontSize: '1.1rem', margin: 0 }}>
                    Sistem Enterprise Securizat (Spring Security + React 19)
                </p>
            </div>

            {/* FORMULAR PANOU */}
            <div style={{ background: '#ffffff', border: '1px solid #E5E7EB', padding: '25px', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05)', marginBottom: '40px' }}>
                <h3 style={{ margin: '0 0 20px 0', color: '#1F2937', fontSize: '1.25rem', fontWeight: '600' }}>Adaugă un Task nou</h3>
                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: '14px' }}>
                        <input
                            type="text"
                            placeholder="Ce ai de făcut astăzi?"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            style={{ width: '100%', padding: '12px 16px', borderRadius: '10px', border: '1px solid #D1D5DB', fontSize: '1rem', boxSizing: 'border-box', outline: 'none' }}
                        />
                    </div>
                    <div style={{ marginBottom: '20px' }}>
                        <textarea
                            placeholder="Adaugă detalii sau descriere (opțional)..."
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            style={{ width: '100%', padding: '12px 16px', borderRadius: '10px', border: '1px solid #D1D5DB', fontSize: '1rem', boxSizing: 'border-box', height: '80px', resize: 'none', outline: 'none' }}
                        />
                    </div>
                    <button type="submit" style={{ background: '#4F46E5', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '10px', fontSize: '1rem', fontWeight: '600', cursor: 'pointer', width: '100%' }}>
                        + Adaugă în listă
                    </button>
                </form>
            </div>

            {/* SECȚIUNE ACTION BUTON PRIORITIZARE AI */}
            {tasks.filter(t => !t.completed).length > 0 && (
                <div style={{ marginBottom: '25px', textAlign: 'right' }}>
                    <button
                        onClick={handleAiPrioritize}
                        disabled={prioritizing}
                        style={{ background: '#EEF2F6', color: '#1E293B', border: '1px solid #CBD5E1', padding: '10px 18px', borderRadius: '10px', fontSize: '0.9rem', fontWeight: '600', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: '8px' }}
                    >
                        {prioritizing ? '⏳ Se analizează lista...' : '🧠 Ordonează Inteligent lista'}
                    </button>
                </div>
            )}

            {/* STRATEGIA DE PRIORITIZARE */}
            {prioritizedList.length > 0 && (
                <div style={{ background: '#F8FAFC', border: '1px solid #E2E8F0', padding: '20px', borderRadius: '14px', marginBottom: '30px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                        <h4 style={{ margin: 0, color: '#0F172A', fontSize: '1.05rem', fontWeight: '700' }}>📋 Planul Sugerat de Executive AI:</h4>
                        <button onClick={() => setPrioritizedList([])} style={{ background: 'none', border: 'none', color: '#64748B', cursor: 'pointer', fontWeight: 'bold' }}>Ascunde</button>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {prioritizedList.map((pTask) => (
                            <div key={pTask.id} style={{ fontSize: '0.9rem', color: '#334155', background: '#fff', padding: '10px 14px', borderRadius: '8px', borderLeft: '4px solid #4F46E5' }}>
                                <strong>#{pTask.priorityOrder} {pTask.title}</strong>
                                <p style={{ margin: '4px 0 0 0', color: '#64748B', fontSize: '0.85rem', fontStyle: 'italic' }}>💡 {pTask.justification}</p>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* SECȚIUNE LISTĂ */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h2 style={{ color: '#1F2937', fontSize: '1.5rem', margin: 0, fontWeight: '700' }}>Task-urile tale</h2>
                <span style={{ background: '#E0E7FF', color: '#4338CA', padding: '4px 12px', borderRadius: '9999px', fontSize: '0.85rem', fontWeight: '650' }}>
                    {tasks.length} {tasks.length === 1 ? 'task' : 'task-uri'}
                </span>
            </div>

            {loading ? (
                <p style={{ textAlign: 'center', color: '#9CA3AF' }}>Se încarcă aplicația...</p>
            ) : tasks.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px', border: '2px dashed #E5E7EB', borderRadius: '16px', color: '#9CA3AF' }}>
                    Nu ai adăugat niciun task încă. Începe prin a completa formularul de mai sus!
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    {Array.isArray(tasks) && [...tasks]
                        .sort((a, b) => Number(a.completed) - Number(b.completed))
                        .map((task) => (
                            <div
                                key={task.id}
                                onClick={() => handleToggle(task.id)}
                                style={{
                                    background: '#ffffff',
                                    padding: '20px',
                                    borderRadius: '14px',
                                    border: '1px solid #E5E7EB',
                                    borderLeft: task.completed ? '6px solid #10B981' : '6px solid #F59E0B',
                                    boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center'
                                }}
                            >
                                <div style={{ paddingRight: '20px', flex: 1 }}>
                                    <h3 style={{ margin: '0 0 6px 0', fontSize: '1.15rem', fontWeight: '600', textDecoration: task.completed ? 'line-through' : 'none', color: task.completed ? '#9CA3AF' : '#111827' }}>
                                        {task.title}
                                    </h3>
                                    {task.description && (
                                        <p style={{ margin: 0, color: task.completed ? '#D1D5DB' : '#4B5563', fontSize: '0.95rem', lineHeight: '1.4' }}>
                                            {task.description}
                                        </p>
                                    )}
                                </div>

                                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    {!task.completed && (
                                        <button
                                            onClick={(e) => handleAiSplit(task.title, task.id, e)}
                                            disabled={aiLoadingId === task.id}
                                            style={{ background: '#F5F3FF', color: '#6D28D9', border: '1px solid #DDD6FE', padding: '6px 12px', borderRadius: '8px', fontSize: '0.8rem', fontWeight: 'bold', cursor: 'pointer' }}
                                        >
                                            {aiLoadingId === task.id ? '⏳...' : '🪄 AI Split'}
                                        </button>
                                    )}

                                    <span style={{ fontSize: '0.8rem', padding: '4px 10px', borderRadius: '9999px', background: task.completed ? '#D1FAE5' : '#FEF3C7', color: task.completed ? '#065F46' : '#92400E', fontWeight: 'bold' }}>
                                        {task.completed ? '✓ Gata' : '⏳ În lucru'}
                                    </span>

                                    <button
                                        onClick={(e) => handleDelete(task.id, e)}
                                        style={{ background: 'none', border: 'none', color: '#EF4444', fontSize: '1.2rem', cursor: 'pointer', padding: '4px 8px' }}
                                    >
                                        🗑️
                                    </button>
                                </div>
                            </div>
                        ))}
                </div>
            )}
        </div>
    );
}

export default App;