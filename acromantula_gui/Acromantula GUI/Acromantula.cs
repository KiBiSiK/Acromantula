using System;
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
            new CodeViewMain {MdiParent = this}.Show();
        }

        private void compareToolStripMenuItem_Click(object sender, EventArgs e)
        {
            new CodeViewComparer {MdiParent = this}.Show();
        }
    }
}