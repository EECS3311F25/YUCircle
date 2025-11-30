export default function useFetch(baseUrl) {
    async function get(url) {
        const res = await fetch(baseUrl + url);
        if (!res.ok) throw new Error(`GET ${url} failed: ${res.status}`);
        return res.json();
    }

    async function post(url, body) {
        const res = await fetch(baseUrl + url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!res.ok) throw new Error(`POST ${url} failed: ${res.status}`);
        return res.json();
    }

    async function patch(url, body) {
        const res = await fetch(baseUrl + url, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (!res.ok) {
            const errorBody = await res.text();
            throw new Error(`PATCH ${url} failed: ${res.status} ${errorBody}`);
        }
        return res.json();
    }

    async function del(url) {
        const res = await fetch(baseUrl + url, { method: "DELETE" });
        if (!res.ok) throw new Error(`DELETE ${url} failed: ${res.status}`);
        return res.json();
    }

    return { get, post, patch, del };
}
