export default function useFetch(baseUrl) {
    function get(url) {
        return fetch(baseUrl + url)
               .then(response => response.json());
    }

    function post(url, body) {
        return fetch(baseUrl + url , {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        })
        .then(response => response.json());
    }

    function put(url, body) {
        return fetch(baseUrl + url, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: body ? JSON.stringify(body) : null
        })
        .then(response => response.json());
    }

    function del(url) {
        return fetch(baseUrl + url, {
            method: "DELETE",
        }).then((response) => response.json());
    }

    return { get, post, put, del };
};
