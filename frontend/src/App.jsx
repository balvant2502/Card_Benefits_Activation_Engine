import { useEffect, useState } from 'react';
import {
  Activity, ArrowRight, BarChart3, Bell, Check, ChevronRight, CreditCard,
  Eye, EyeOff, LayoutDashboard, LogOut, LockKeyhole, Mail, Menu, Plus,
  ReceiptText, ShieldCheck, Sparkles, Tags, UserRound, X, Zap,
} from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8090';
const tokenKey = 'benefits_engine_token';

async function api(path, options = {}) {
  const token = localStorage.getItem(tokenKey) || sessionStorage.getItem(tokenKey);
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });
  const text = await response.text();
  let data = {};
  try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }
  if (!response.ok) throw new Error(data.message || data.error || `Request failed (${response.status})`);
  return data;
}

function Logo() {
  return <div className="brand-mark"><Sparkles size={17} strokeWidth={2.5} /> NEXA</div>;
}

function Auth({ onAuthenticated }) {
  const [isRegistering, setIsRegistering] = useState(false);
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', address: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [status, setStatus] = useState({ type: '', message: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const updateField = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
    if (status.message) setStatus({ type: '', message: '' });
  };

  const submit = async (event) => {
    event.preventDefault();
    if (isRegistering && !/^\d{10}$/.test(form.phone)) {
      setStatus({ type: 'error', message: 'Phone number must contain exactly 10 digits.' });
      return;
    }
    setIsSubmitting(true);
    setStatus({ type: '', message: '' });
    try {
      const data = await api(`/api/auth/${isRegistering ? 'register' : 'login'}`, {
        method: 'POST',
        body: JSON.stringify(form),
      });
      const storage = rememberMe ? localStorage : sessionStorage;
      storage.setItem(tokenKey, data.token);
      onAuthenticated();
    } catch (error) {
      setStatus({ type: 'error', message: error.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="brand-panel" aria-label="Nexa Benefits overview">
        <Logo />
        <div className="brand-copy"><p className="eyebrow">Benefits, activated</p><h1>Make every<br /><em>purchase</em> count.</h1><p className="brand-description">One calm place to track your card benefits, activate eligible claims, and stay ahead of what is yours.</p></div>
        <div className="proof-row"><div className="proof-icon"><Check size={17} /></div><span>Secure access to your benefits dashboard</span></div>
        <div className="panel-orbit orbit-one" /><div className="panel-orbit orbit-two" /><div className="panel-grid" />
      </section>
      <section className="form-panel"><div className="form-wrap">
        <div className="mobile-brand"><Logo /></div>
        <div className="form-heading"><p className="eyebrow">Member portal</p><h2>{isRegistering ? 'Join Nexa.' : 'Welcome back.'}</h2><p>{isRegistering ? 'Create your account and start activating your benefits.' : 'Sign in to see what your cards can do for you.'}</p></div>
        <form onSubmit={submit} noValidate>
          {isRegistering && <Field label="Full name" name="name" value={form.name} onChange={updateField} placeholder="Jane Doe" autoComplete="name" />}
          <Field label="Email address" name="email" value={form.email} onChange={updateField} placeholder="you@example.com" type="email" autoComplete="email" icon={<Mail size={18} />} />
          {isRegistering && <><Field label="Phone number" name="phone" value={form.phone} onChange={updateField} placeholder="9876543210" type="tel" inputMode="numeric" maxLength="10" pattern="[0-9]{10}" autoComplete="tel" spaced /><Field label="Address" name="address" value={form.address} onChange={updateField} placeholder="1 Main Street" autoComplete="street-address" spaced /></>}
          <div className={isRegistering ? 'field-spaced' : 'label-row'}><label htmlFor="password">Password</label>{!isRegistering && <a href="#forgot-password">Forgot password?</a>}</div>
          <div className="input-shell"><LockKeyhole size={18} aria-hidden="true" /><input id="password" name="password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" placeholder="Enter your password" value={form.password} onChange={updateField} required minLength="6" /><button className="icon-button" type="button" onClick={() => setShowPassword((visible) => !visible)} aria-label="Toggle password visibility">{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></div>
          {!isRegistering && <label className="checkbox-row"><input type="checkbox" checked={rememberMe} onChange={(event) => setRememberMe(event.target.checked)} /><span className="checkmark"><Check size={12} /></span>Keep me signed in</label>}
          {status.message && <div className={`status ${status.type}`} role="alert">{status.message}</div>}
          <button className="submit-button" type="submit" disabled={isSubmitting}>{isSubmitting ? (isRegistering ? 'Creating account...' : 'Signing you in...') : (isRegistering ? 'Create account' : 'Sign in')}{!isSubmitting && <ArrowRight size={19} />}</button>
        </form>
        <div className="security-note"><ShieldCheck size={17} /> Your information is encrypted and protected.</div>
        <p className="create-account">{isRegistering ? 'Already have an account?' : 'New to Nexa?'}{' '}<a href="#switch" onClick={(event) => { event.preventDefault(); setIsRegistering((current) => !current); setStatus({ type: '', message: '' }); }}>{isRegistering ? 'Sign in' : 'Create an account'} <ArrowRight size={14} /></a></p>
      </div></section>
    </main>
  );
}

function Field({ label, name, value, onChange, placeholder, type = 'text', inputMode, maxLength, pattern, autoComplete, icon, spaced }) {
  return <div className={spaced ? 'field-spaced' : ''}><label htmlFor={name}>{label}</label><div className="input-shell">{icon}<input id={name} name={name} type={type} inputMode={inputMode} maxLength={maxLength} pattern={pattern} autoComplete={autoComplete} placeholder={placeholder} value={value} onChange={onChange} required /></div></div>;
}

function App() {
  const [token, setToken] = useState(() => localStorage.getItem(tokenKey) || sessionStorage.getItem(tokenKey));
  return token ? <Dashboard onSignOut={() => { localStorage.removeItem(tokenKey); sessionStorage.removeItem(tokenKey); setToken(null); }} /> : <Auth onAuthenticated={() => setToken(localStorage.getItem(tokenKey) || sessionStorage.getItem(tokenKey))} />;
}

function Dashboard({ onSignOut }) {
  const [user, setUser] = useState(null);
  const [cards, setCards] = useState([]);
  const [benefits, setBenefits] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [claims, setClaims] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [profile, setProfile] = useState(null);
  const [openMenu, setOpenMenu] = useState('');
  const [view, setView] = useState('overview');
  const [mobileNav, setMobileNav] = useState(false);
  const [loading, setLoading] = useState(true);
  const [action, setAction] = useState({ type: '', message: '' });
  const [swipeCardId, setSwipeCardId] = useState(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const currentUser = await api('/api/auth/me');
      const [cardData, benefitData, transactionData, claimData, notificationData, profileData] = await Promise.all([
        api('/api/cards/me'),
        api('/api/benefits/active').catch(() => []),
        api(`/api/transactions/user/${currentUser.userId}`).catch(() => []),
        api(`/api/claims/user/${currentUser.userId}`).catch(() => []),
        api(`/api/claim-notifications/user/${currentUser.userId}`).catch(() => []),
        api('/api/auth/profile').catch(() => currentUser),
      ]);
      setUser(currentUser); setProfile(profileData); setCards(cardData); setBenefits(benefitData); setTransactions(transactionData); setClaims(claimData); setNotifications(notificationData);
    } catch (error) {
      setAction({ type: 'error', message: error.message });
      if (/401|403|authenticated/i.test(error.message)) onSignOut();
    } finally { setLoading(false); }
  };

  const markNotificationRead = async (notification) => {
    if (!notification.read) {
      await api(`/api/claim-notifications/${notification.id}/read`, { method: 'PATCH' });
      setNotifications((current) => current.map((item) => item.id === notification.id ? { ...item, read: true } : item));
    }
  };

  useEffect(() => { loadData(); }, []);

  const simulateSwipe = async (cardId) => {
    setAction({ type: '', message: '' });
    try {
      const transaction = await api('/api/transactions/dummy-swipe', { method: 'POST', body: JSON.stringify({ cardId }) });
      setAction({ type: 'success', message: transaction.isEligible ? 'Eligible benefit found. Review your claim below.' : 'Transaction captured and eligibility checked.' });
      await loadData();
      if (transaction.isEligible) setView('claims');
    }
    catch (error) { setAction({ type: 'error', message: error.message }); }
    finally { setSwipeCardId(null); }
  };

  const createCard = async () => {
    setAction({ type: '', message: '' });
    try {
      await api('/api/cards/me', { method: 'POST' });
      setAction({ type: 'success', message: 'New virtual card created.' });
      await loadData();
    } catch (error) { setAction({ type: 'error', message: error.message }); }
  };

  const claimBenefit = async (claim) => {
    setAction({ type: '', message: '' });
    try {
      await api(`/api/claims/${claim.id}/activate`, { method: 'PUT' });
      await api(`/api/claims/${claim.id}/submit`, {
        method: 'PUT',
        body: JSON.stringify({ submissionData: claim.prefilledData || '{}' }),
      });
      setAction({ type: 'success', message: 'Benefit claim submitted for review.' });
      await loadData();
      setView('claims');
    } catch (error) { setAction({ type: 'error', message: error.message }); }
  };

  const navItems = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'cards', label: 'My cards', icon: CreditCard },
    { id: 'benefits', label: 'Benefits', icon: Tags },
    { id: 'activity', label: 'Activity', icon: Activity },
    { id: 'claims', label: 'Claims', icon: ReceiptText },
  ];

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'open' : ''}`}>
      <div className="sidebar-top"><Logo /><button className="sidebar-close icon-button" onClick={() => setMobileNav(false)} aria-label="Close navigation"><X size={20} /></button></div>
      <div className="workspace-label">PERSONAL WORKSPACE</div>
      <nav>{navItems.map(({ id, label, icon: Icon }) => <button key={id} className={view === id ? 'nav-item active' : 'nav-item'} onClick={() => { setView(id); setMobileNav(false); }}><Icon size={18} />{label}</button>)}</nav>
      <div className="sidebar-bottom"><div className="support-line"><ShieldCheck size={16} /><span>Protected account</span></div><button className="nav-item" onClick={onSignOut}><LogOut size={18} />Sign out</button></div>
    </aside>
    {mobileNav && <button className="nav-backdrop" onClick={() => setMobileNav(false)} aria-label="Close navigation" />}
    <div className="main-shell">
      <header className="topbar"><button className="menu-button icon-button" onClick={() => setMobileNav(true)} aria-label="Open navigation"><Menu size={22} /></button><div className="topbar-context">{navItems.find((item) => item.id === view)?.label}</div><div className="topbar-actions"><div className="topbar-menu"><button className="round-action notification-button" aria-label="Notifications" onClick={() => setOpenMenu(openMenu === 'notifications' ? '' : 'notifications')}><Bell size={18} />{notifications.some((item) => !item.read) && <span className="notification-dot" />}</button>{openMenu === 'notifications' && <NotificationMenu notifications={notifications} onRead={markNotificationRead} />}</div><div className="topbar-menu"><button className="avatar avatar-button" aria-label="Open profile" onClick={() => setOpenMenu(openMenu === 'profile' ? '' : 'profile')}>{user?.name?.charAt(0) || 'V'}</button>{openMenu === 'profile' && <ProfileMenu profile={profile || user} onSignOut={onSignOut} />}</div><div className="topbar-name">{user?.name || 'Member'}</div></div></header>
      <main className="dashboard-content">
        {loading ? <div className="loading-state"><div className="loading-dot" />Loading your benefits workspace...</div> : <>
          <div className="dashboard-heading"><div><p className="eyebrow">{greeting()}, {user?.name?.split(' ')[0] || 'member'}</p><h2>{view === 'overview' ? 'Your benefits, at a glance.' : navItems.find((item) => item.id === view)?.label}</h2><p className="muted">{view === 'overview' ? 'Stay close to the value your cards unlock.' : 'Review and manage your benefits activity.'}</p></div><div className="heading-actions">{view === 'overview' && <button className="primary-action" onClick={() => setSwipeCardId('choose')}><Plus size={17} />Simulate transaction</button>}{view === 'cards' && <button className="primary-action" onClick={createCard}><Plus size={17} />Create virtual card</button>}</div></div>
          {action.message && <div className={`status ${action.type}`}>{action.message}</div>}
          {view === 'overview' && <Overview cards={cards} benefits={benefits} transactions={transactions} claims={claims} onView={setView} />}
          {view === 'cards' && <CardsView cards={cards} />}
          {view === 'benefits' && <BenefitsView benefits={benefits} />}
          {view === 'activity' && <ActivityView transactions={transactions} />}
          {view === 'claims' && <ClaimsView claims={claims} onClaim={claimBenefit} />}
        </>}
      </main>
    </div>
    {swipeCardId && <CardPicker cards={cards} onClose={() => setSwipeCardId(null)} onSelect={(cardId) => simulateSwipe(cardId)} />}
  </div>;
}

function CardPicker({ cards, onClose, onSelect }) {
  return <div className="modal-backdrop" role="presentation"><div className="modal" role="dialog" aria-modal="true" aria-labelledby="swipe-title"><button className="modal-close icon-button" onClick={onClose} aria-label="Close"><X size={19} /></button><p className="eyebrow">Card activity</p><h3 id="swipe-title">Choose a card to swipe</h3><p className="modal-copy">The transaction and benefit eligibility check will use this card.</p><div className="picker-list">{cards.filter((card) => card.active).map((card) => <button className="picker-card" key={card.id} onClick={() => onSelect(card.id)}><span className="picker-card-brand">{card.network || 'VISA'}</span><span>•••• {card.last4 || '0000'}</span><ChevronRight size={17} /></button>)}</div>{!cards.length && <Empty icon={CreditCard} text="Create a card before simulating a transaction." />}</div></div>;
}

function NotificationMenu({ notifications, onRead }) {
  return <div className="popover notification-popover"><div className="popover-heading"><strong>Notifications</strong><span>{notifications.filter((item) => !item.read).length} new</span></div>{notifications.length ? notifications.slice(0, 5).map((notification) => <button className={notification.read ? 'notification-item read' : 'notification-item'} key={notification.id} onClick={() => onRead(notification)}><span className="notification-icon"><Bell size={14} /></span><span><strong>{notification.benefitName || 'Eligible benefit'}</strong><small>{notification.message}</small></span></button>) : <p className="popover-empty">No benefit notifications yet.</p>}</div>;
}

function ProfileMenu({ profile, onSignOut }) {
  return <div className="popover profile-popover"><div className="profile-heading"><div className="profile-avatar">{profile?.name?.charAt(0) || 'V'}</div><div><strong>{profile?.name || 'Member'}</strong><small>{profile?.role || 'CUSTOMER'}</small></div></div><div className="profile-details"><span><Mail size={14} />{profile?.email || 'No email'}</span><span><UserRound size={14} />{profile?.phone || 'No phone added'}</span><span><Activity size={14} />{profile?.address || 'No address added'}</span></div><button className="profile-signout" onClick={onSignOut}><LogOut size={15} />Sign out</button></div>;
}

function greeting() { const hour = new Date().getHours(); return hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'; }
function Overview({ cards, benefits, transactions, claims, onView }) {
  const eligible = transactions.filter((item) => item.isEligible).length;
  const activeClaims = claims.filter((item) => !['APPROVED', 'REJECTED'].includes(item.status)).length;
  return <>
    <section className="metric-grid"><Metric icon={CreditCard} label="Active cards" value={cards.filter((card) => card.active).length} tone="green" /><Metric icon={Zap} label="Benefits available" value={benefits.length} tone="gold" /><Metric icon={BarChart3} label="Eligible purchases" value={eligible} tone="blue" /><Metric icon={ReceiptText} label="Open claims" value={activeClaims} tone="coral" /></section>
    <section className="overview-grid"><div className="section-block"><SectionHeader title="Your cards" action="View all" onClick={() => onView('cards')} />{cards.length ? <div className="card-list">{cards.slice(0, 2).map((card) => <CardTile key={card.id} card={card} />)}</div> : <Empty icon={CreditCard} text="Your virtual card will appear here after registration." />}</div><div className="section-block"><SectionHeader title="Latest activity" action="See activity" onClick={() => onView('activity')} />{transactions.length ? <div className="activity-list">{transactions.slice(0, 4).map((item) => <TransactionRow key={item.id} item={item} />)}</div> : <Empty icon={Activity} text="Your card activity will show up here." />}</div></section>
    <section className="section-block benefits-strip"><SectionHeader title="Benefits waiting for you" action="Explore benefits" onClick={() => onView('benefits')} /><div className="benefit-mini-grid">{benefits.slice(0, 3).map((benefit) => <BenefitTile key={benefit.id} benefit={benefit} />)}</div></section>
  </>;
}
function Metric({ icon: Icon, label, value, tone }) { return <div className="metric-card"><div className={`metric-icon ${tone}`}><Icon size={18} /></div><div><span>{label}</span><strong>{value}</strong></div></div>; }
function SectionHeader({ title, action, onClick }) { return <div className="section-header"><h3>{title}</h3>{action && <button onClick={onClick}>{action}<ChevronRight size={15} /></button>}</div>; }
function CardTile({ card }) { return <div className="card-tile"><div className="card-chip" /><div className="card-network">{card.network || 'VISA'} <span>{card.virtual ? 'VIRTUAL' : 'CARD'}</span></div><div className="card-number">•••• &nbsp;•••• &nbsp;•••• &nbsp;{card.last4 || '0000'}</div><div className="card-footer"><span>NEXA MEMBER</span><span>{card.active ? 'ACTIVE' : 'INACTIVE'}</span></div></div>; }
function TransactionRow({ item }) { return <div className="transaction-row"><div className="transaction-icon"><ReceiptText size={16} /></div><div className="transaction-main"><strong>{item.merchant || 'Card purchase'}</strong><span>{item.category || 'Purchase'} · {formatDate(item.txnDate || item.createdAt)}</span></div><div className="transaction-result">{item.isEligible ? <><Check size={14} /> Eligible</> : <span>-${formatAmount(item.amount)}</span>}</div></div>; }
function BenefitTile({ benefit }) { return <div className="benefit-tile"><div className="benefit-symbol"><Sparkles size={17} /></div><div><strong>{benefit.name || benefit.benefitType || 'Card benefit'}</strong><p>{benefit.description || 'A benefit ready to be activated.'}</p></div></div>; }
function CardsView({ cards }) { return <div className="view-grid">{cards.map((card) => <CardTile key={card.id} card={card} />)}{!cards.length && <Empty icon={CreditCard} text="No cards found. Create your first virtual card above." />}</div>; }
function BenefitsView({ benefits }) { return <div className="benefits-grid">{benefits.map((benefit) => <div className="benefit-card" key={benefit.id}><div className="benefit-symbol"><Sparkles size={20} /></div><p className="eyebrow">{benefit.applicableCategories || 'CARD BENEFIT'}</p><h3>{benefit.name || 'Available benefit'}</h3><p>{benefit.description || 'Use this benefit on eligible purchases.'}</p>{benefit.minAmount && <span className="benefit-rule">Minimum purchase ${formatAmount(benefit.minAmount)}</span>}</div>)}{!benefits.length && <Empty icon={Tags} text="No active benefits found." />}</div>; }
function ActivityView({ transactions }) { return <div className="table-card"><div className="section-header"><h3>Transaction history</h3><span className="muted">{transactions.length} records</span></div>{transactions.length ? transactions.map((item) => <TransactionRow key={item.id} item={item} />) : <Empty icon={Activity} text="No transactions found." />}</div>; }
function ClaimsView({ claims, onClaim }) { return <div className="table-card"><div className="section-header"><h3>Claims</h3><span className="muted">{claims.length} total</span></div>{claims.length ? claims.map((claim) => <div className="claim-row" key={claim.id}><div><strong>{claim.benefit?.name || claim.benefit?.benefitType || 'Benefit claim'}</strong><span>Claim #{claim.id} · {formatDate(claim.createdAt)}</span></div><div className="claim-actions"><span className={`claim-status ${String(claim.status).toLowerCase()}`}>{claim.status}</span>{['ELIGIBLE', 'ACTIVATED'].includes(claim.status) && <button className="claim-button" onClick={() => onClaim(claim)}><Zap size={13} />Claim benefit</button>}</div></div>) : <Empty icon={ReceiptText} text="No claims found. Eligible purchases will appear here." />}</div>; }
function Empty({ icon: Icon, text }) { return <div className="empty-state"><Icon size={24} /><span>{text}</span></div>; }
function formatDate(value) { if (!value) return 'Recently'; const date = new Date(value); return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }); }
function formatAmount(value) { return Number(value || 0).toFixed(2); }

export default App;
