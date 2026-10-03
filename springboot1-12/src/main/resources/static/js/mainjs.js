$(document).ready(function () {

    $('#Login').click(function () {

        var email = $('#email').val();
        var password = $('#password').val();

        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            data: basicInfo,

            success: function (data) {

                 localStorage.setItem("token", data.token);

                window.location.href = "/profile";
            },

            error: function (e) {
                console.log(e);
                alert("Login Failed");
            }
        });

    });


    if (localStorage.getItem("token")) {

        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',

            beforeSend: function (xhr) {

                xhr.setRequestHeader(
                    'Authorization',
                    'Bearer ' + localStorage.getItem("token")
                );

            },

            success: function (data) {

                console.log(data);

                $('#profile').html(data.fullName);

                if (data.images) {
                    $('#images').attr('src', data.images);
                }
            },

            error: function (e) {

                console.log(e);

                // Token hết hạn / không hợp lệ
                localStorage.removeItem("token");

                window.location.href = "/login";
            }
        });
    }


    $('#logout').click(function () {

        localStorage.removeItem("token");

        window.location.href = "/login";

    });

});