import { useEffect, useState } from 'react';

interface Task {
    id: number;
    title: string;
    description: string;
    completed: boolean;
}

function App() {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [loading, setLoading] = useState(true);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');

    const fetchTasks = () => {
        fetch('/api/tasks')
            .then((res) => res.json())
            .then((data) => {
                setTasks(data);
                setLoading(false);
            })
            .catch((err) => console.error("Eroare:", err));
    };

    useEffect(() => {
        fetchTasks();
    }, []);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!title.trim()) return;

        const newTask = { title, description };

        fetch('/api/tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newTask),
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
        fetch(`/api/tasks/${id}/toggle`, { method: 'PUT' })
            .then((res) => res.json())
            .then((data) => setTasks(data))
            .catch((err) => console.error("Eroare la actualizare:", err));
    };

    const handleDelete = (id: number, e: React.MouseEvent) => {
        e.stopPropagation(); // Previne bifarea task-ului când se dă click pe ștergere
        if (window.confirm("Sigur vrei să ștergi acest task?")) {
            fetch(`/api/tasks/${id}`, { method: 'DELETE' })
                .then((res) => res.json())
                .then((data) => setTasks(data))
                .catch((err) => console.error("Eroare la ștergere:", err));
        }
    };

    return (
        <div style={{ maxWidth: '650px', margin: '50px auto', fontFamily: '"Segoe UI", Roboto, Helvetica, Arial, sans-serif', padding: '0 20px' }}>

            {/* HEADER */}
            <div style={{ textAlign: 'center', marginBottom: '40px' }}>
                <h1 style={{ color: '#4F46E5', fontSize: '2.5rem', fontWeight: '800', margin: '0 0 5px 0', letterSpacing: '-0.025em' }}>
                    TaskBuddy <span style={{ fontSize: '2rem' }}>🚀</span>
                </h1>
                <p style={{ color: '#4B5563', fontSize: '1.1rem', margin: 0 }}>
                    Sistem de Gestiune Full-Stack (Spring Boot 3 + PostgreSQL + React 19)
                </p>
            </div>

            {/* FORMULAR PANOU */}
            <div style={{ background: '#ffffff', border: '1px solid #E5E7EB', padding: '25px', borderRadius: '16px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.06)', marginBottom: '40px' }}>
                <h3 style={{ margin: '0 0 20px 0', color: '#1F2937', fontSize: '1.25rem', fontWeight: '600' }}>Adaugă un Task nou</h3>
                <form onSubmit={handleSubmit}>
                    <div style={{ marginBottom: '14px' }}>
                        <input
                            type="text"
                            placeholder="Ce ai de făcut astăzi?"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            style={{ width: '100%', padding: '12px 16px', borderRadius: '10px', border: '1px solid #D1D5DB', fontSize: '1rem', boxSizing: 'border-box', outline: 'none', transition: 'border-color 0.2s' }}
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
                    <button type="submit" style={{ background: '#4F46E5', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '10px', fontSize: '1rem', fontWeight: '600', cursor: 'pointer', width: '100%', boxShadow: '0 2px 4px rgba(79, 70, 229, 0.2)', transition: 'background-color 0.2s' }}>
                        + Adaugă în listă
                    </button>
                </form>
            </div>

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
                                alignItems: 'center',
                                transition: 'all 0.2s ease-in-out'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = 'translateY(-2px)';
                                e.currentTarget.style.boxShadow = '0 4px 6px -1px rgba(0,0,0,0.1)';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = 'none';
                                e.currentTarget.style.boxShadow = '0 1px 2px rgba(0,0,0,0.05)';
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
                <span style={{ fontSize: '0.8rem', padding: '4px 10px', borderRadius: '9999px', background: task.completed ? '#D1FAE5' : '#FEF3C7', color: task.completed ? '#065F46' : '#92400E', fontWeight: 'bold', whiteSpace: 'nowrap' }}>
                  {task.completed ? '✓ Gata' : '⏳ În lucru'}
                </span>

                                {/* BUTONUL DE ȘTERGERE */}
                                <button
                                    onClick={(e) => handleDelete(task.id, e)}
                                    style={{ background: 'none', border: 'none', color: '#EF4444', fontSize: '1.2rem', cursor: 'pointer', padding: '4px 8px', borderRadius: '6px', transition: 'background-color 0.2s', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#FEE2E2'}
                                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                    title="Șterge task"
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