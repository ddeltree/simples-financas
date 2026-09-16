import { useEffect } from "react";
import { getAuthSnapshot, subscribeAuth } from "#/store/auth-store";
import { setStorageScope } from "#/store/finance-store";

/**
 * A única peça que conhece as duas stores. A financeira não sabe o que é uma conta e a
 * de autenticação não sabe o que é um orçamento; a ponte traduz "quem entrou" em "qual
 * gaveta do localStorage abrir".
 */
let started = false;

function syncScope(): void {
	const { status, user } = getAuthSnapshot();
	setStorageScope(status === "signed-in" && user !== null ? user.id : null);
}

export function startSessionBridge(): void {
	if (started || typeof window === "undefined") return;

	started = true;
	subscribeAuth(syncScope);
	syncScope();
}

export function useSessionBridge(): void {
	useEffect(startSessionBridge, []);
}
