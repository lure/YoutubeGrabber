kea = function (a) {
    a = a.split("");
    var b = [16668072, null, function (c, d) {
        for (var e = 64, f = []; ++e - f.length - 32;) {
            switch (e) {
                case 91:
                    e = 44;
                    continue;
                case 123:
                    e = 65;
                    break;
                case 65:
                    e -= 18;
                    continue;
                case 58:
                    e = 96;
                    continue;
                case 46:
                    e = 95
            }
            f.push(String.fromCharCode(e))
        }
        c.forEach(function (k, l, m) {
            this.push(m[l] = f[(f.indexOf(k) - f.indexOf(this[l]) + l - 32 + e--) % f.length])
        }, d.split(""))
    },
        -1126544007, 737521820, -676102706, 1525460130, 1631774164, 1242327097, 366259048, -917177250, -513790427, -1016326686, function (c) {
            c.reverse()
        },
        null, -1295937155, null, 1843018106, -631178905, -22644338, 1358457420, -2030607609, 268531754, -622237946, -414492350, -940504152, function (c, d) {
            d = (d % c.length + c.length) % c.length;
            c.splice(0, 1, c.splice(d, 1, c[0])[0])
        },
        -2143077847, -1531223302, a, -1228283886, "catch", a, function (c, d) {
            for (d = (d % c.length + c.length) % c.length; d--;) c.unshift(c.pop())
        },
        -414492350, function (c) {
            c.reverse()
        },
        function (c, d) {
            c.push(d)
        },
        1926931404, 1806802669, -583102924, a, 1525460130, function (c, d) {
            d = (d % c.length + c.length) % c.length;
            var e = c[0];
            c[0] = c[d];
            c[d] = e
        },
        -1524061202, function (c, d) {
            d = (d % c.length + c.length) % c.length;
            c.splice(-d).reverse().forEach(function (e) {
                c.unshift(e)
            })
        },
        function (c, d) {
            d = (d % c.length + c.length) % c.length;
            c.splice(d, 1)
        },
        -243291836, function (c) {
            for (var d = c.length; d;) c.push(c.splice(--d, 1)[0])
        },
        -349289097, 863756783, -495944936];
    b[1] = b;
    b[14] = b;
    b[16] = b;
    b[42](b[16], b[17]);
    b[44](b[32], b[9]);
    b[0](b[14]);
    b[8](b[36], b[7]);
    b[24](b[21], b[46]);
    b[8](b[49], b[4]);
    b[8](b[10], b[23]);
    b[24](b[36], b[2]);
    b[37](b[21]);
    b[8](b[49], b[13]);
    b[24](b[49], b[43]);
    b[37](b[49]);
    b[36](b[24], b[21]);
    b[33](b[24], b[3]);
    b[1](b[49], b[15]);
    b[51](b[38], b[24]);
    b[9](b[33]);
    b[0](b[2], b[23]);
    b[16](b[18], b[31]);
    b[9](b[2], b[43]);
    b[45](b[13], b[47]);
    b[50](b[10], b[34]);
    b[40](b[13], b[11]);
    b[9](b[10], b[17]);
    b[9](b[13], b[4]);
    b[45](b[10], b[22]);
    b[45](b[18], b[32]);
    b[0](b[41], b[14]);
    b[11](b[43], b[0]);
    b[31](b[40], b[25]);
    b[41](b[47]);
    b[31](b[44], b[1]);
    b[34](b[23], b[2]);
    b[1](b[5], b[45]);
    b[37](b[18], b[36]);
    b[37](b[10], b[22]);
    b[44](b[46], b[4]);
    b[1](b[5], b[16]);
    b[37](b[18], b[29]);
    return a.join("")
};