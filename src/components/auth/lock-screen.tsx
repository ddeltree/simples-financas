import { LoaderCircle, Lock } from "lucide-react";
import { type FormEvent, useId, useState } from "react";
import { MIN_PASSWORD_LENGTH } from "#/domain/auth/types";
import {
	clearAuthError,
	register,
	signIn,
	useAuthState,
} from "#/store/auth-store";

type Mode = "signin" | "register";

export function LockScreen() {
	const { accounts, pending, error } = useAuthState();
	const nameId = useId();
	const passwordId = useId();

	const [mode, setMode] = useState<Mode>(
		accounts.length === 0 ? "register" : "signin",
	);
	const [name, setName] = useState(accounts[0]?.name ?? "");
	const [password, setPassword] = useState("");
	const [remember, setRemember] = useState(false);

	function switchMode(next: Mode) {
		if (next === mode) return;
		setMode(next);
		setPassword("");
		clearAuthError();
	}

	async function onSubmit(event: FormEvent<HTMLFormElement>) {
		event.preventDefault();
		if (pending) return;

		const action = mode === "signin" ? signIn : register;
		const ok = await action({ name, password, remember });
		if (ok) setPassword("");
	}

	return (
		<main className="page-wrap flex min-h-dvh flex-col items-center justify-center py-10">
			<section className="island-shell rise-in w-full max-w-sm rounded-3xl p-7 sm:p-8">
				<div className="grid size-12 place-items-center rounded-2xl bg-palm-soft text-palm">
					<Lock className="size-6" />
				</div>

				<p className="island-kicker mt-5">Finanças como tarefas</p>
				<h1 className="font-display text-2xl font-bold text-ink">
					Simples Finanças
				</h1>
				<p className="mt-2 text-sm text-ink-soft">
					{mode === "signin"
						? "Entre para ver o orçamento deste mês."
						: "Crie uma conta neste dispositivo para guardar suas tarefas."}
				</p>

				<div className="mt-6 flex gap-1.5 rounded-xl border border-line bg-[var(--chip-bg)] p-1">
					<ModeTab
						active={mode === "signin"}
						onClick={() => switchMode("signin")}
						disabled={accounts.length === 0}
					>
						Entrar
					</ModeTab>
					<ModeTab
						active={mode === "register"}
						onClick={() => switchMode("register")}
					>
						Criar conta
					</ModeTab>
				</div>

				<form onSubmit={onSubmit} className="mt-5 flex flex-col gap-4">
					<Field id={nameId} label="Nome">
						<input
							id={nameId}
							value={name}
							onChange={(event) => setName(event.target.value)}
							autoComplete="username"
							// biome-ignore lint/a11y/noAutofocus: é o primeiro campo da única tela visível.
							autoFocus
							required
							className="w-full rounded-xl border border-line bg-[var(--surface-strong)] px-3 py-2.5 text-ink outline-none focus:border-lagoon-deep"
						/>
					</Field>

					<Field
						id={passwordId}
						label="Senha"
						hint={
							mode === "register"
								? `Mínimo de ${MIN_PASSWORD_LENGTH} caracteres`
								: undefined
						}
					>
						<input
							id={passwordId}
							type="password"
							value={password}
							onChange={(event) => setPassword(event.target.value)}
							autoComplete={
								mode === "signin" ? "current-password" : "new-password"
							}
							required
							className="w-full rounded-xl border border-line bg-[var(--surface-strong)] px-3 py-2.5 text-ink outline-none focus:border-lagoon-deep"
						/>
					</Field>

					<label className="flex cursor-pointer items-center gap-2 text-sm text-ink-soft">
						<input
							type="checkbox"
							checked={remember}
							onChange={(event) => setRemember(event.target.checked)}
							className="size-4 accent-[var(--palm)]"
						/>
						Manter conectado neste dispositivo
					</label>

					{error && (
						<p role="alert" className="text-sm font-medium text-coral">
							{error.message}
						</p>
					)}

					<button
						type="submit"
						disabled={pending}
						className="inline-flex items-center justify-center gap-2 rounded-xl bg-ink px-5 py-2.5 text-sm font-semibold text-[var(--foam)] hover:opacity-90 disabled:opacity-60"
					>
						{pending && <LoaderCircle className="size-4 animate-spin" />}
						{pending
							? "Só um instante…"
							: mode === "signin"
								? "Entrar"
								: "Criar conta e entrar"}
					</button>
				</form>

				<p className="mt-5 text-xs leading-relaxed text-ink-soft">
					A senha fica só neste dispositivo e nunca sai dele. Ela tranca o app
					contra quem pega o aparelho — não protege os dados de quem abre as
					ferramentas do navegador.
				</p>
			</section>
		</main>
	);
}

function ModeTab({
	active,
	disabled,
	onClick,
	children,
}: {
	active: boolean;
	disabled?: boolean;
	onClick: () => void;
	children: React.ReactNode;
}) {
	return (
		<button
			type="button"
			onClick={onClick}
			disabled={disabled}
			aria-pressed={active}
			className={
				active
					? "flex-1 rounded-lg bg-ink px-3 py-1.5 text-sm font-semibold text-[var(--foam)]"
					: "flex-1 rounded-lg px-3 py-1.5 text-sm font-semibold text-ink-soft hover:text-ink disabled:opacity-40"
			}
		>
			{children}
		</button>
	);
}

function Field({
	id,
	label,
	hint,
	children,
}: {
	id: string;
	label: string;
	hint?: string;
	children: React.ReactNode;
}) {
	return (
		<div className="flex flex-col gap-1.5">
			<label htmlFor={id} className="text-sm font-semibold text-ink">
				{label}
			</label>
			{children}
			{hint && <span className="text-xs text-ink-soft">{hint}</span>}
		</div>
	);
}
