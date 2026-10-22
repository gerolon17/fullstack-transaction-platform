import { TransactionList } from './components/TransactionList';
import { TransactionForm } from './components/TransactionForm';

function App() {
  return (
    <main className="app-container">
      <h1 className="header-title">Tenpo FullStack Challenge</h1>
      <TransactionForm />
      <TransactionList />
    </main>
  );
}

export default App;