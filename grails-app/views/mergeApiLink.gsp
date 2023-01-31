<html>
<head>
    <script src="https://unpkg.com/axios/dist/axios.min.js"></script>
    <script>
        const baseURL = 'http://localhost:8080/'
        const storeAccountToken = async (payload) => {
            const response = await axios.post('http://localhost:8080/mergeApi/saveAccountToken', payload)
            console.log(response)
        }
    </script>
    <title></title>
</head>

<body>
    <button id="open-link-button">Start linking</button>
    <script src="https://cdn.merge.dev/initialize.js"></script>
    <script type="text/javascript">
        const button = document.getElementById("open-link-button");
        button.disabled = true;

        function onSuccess(public_token) {
            // Send public_token to server (Step 3)
            console.log(public_token)
            storeAccountToken({
                publicToken: public_token,
                getStructureUserId: '${getStructureUserId}',
                integrationType: '${integrationType}',
                customerSoftwareId: '${customerSoftwareId}'
            })
        }

        MergeLink.initialize({
            // Replace ADD_GENERATED_LINK_TOKEN with the token retrieved from your backend (Step 1)
            linkToken: "${linkToken}",
            onSuccess: (public_token) => onSuccess(public_token),
            onReady: () => (button.disabled = false),
            // A value of `true` for `shouldSendTokenOnSuccessfulLink` makes Link call `onSuccess`
            // immediately after an account has been successfully linked instead of after the user
            // closes the Link modal.
            shouldSendTokenOnSuccessfulLink: true,
            // tenantConfig: {
            // apiBaseURL: "https://api-eu.merge.dev" /* OR your specified single tenant API base URL */
            // },
        });

        button.addEventListener("click", function () {
            MergeLink.openLink();
        });
    </script>
</body>
</html>
