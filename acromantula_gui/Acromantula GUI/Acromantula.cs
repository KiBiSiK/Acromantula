using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using Acromantula_GUI.CodeView;

namespace Acromantula_GUI
{
    public partial class Acromantula : Form
    {
        public Acromantula()
        {
            InitializeComponent();
        }

        private void newWindowToolStripMenuItem_Click(object sender, EventArgs e)
        {
            var test = new CodeViewMain();
            test.MdiParent = this;
            test.Show();
        }

        private void compareToolStripMenuItem_Click(object sender, EventArgs e)
        {
            var test = new CodeViewComparer();
            test.MdiParent = this;
            test.Show();
        }
    }
}
